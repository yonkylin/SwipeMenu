package yonky.swipemenu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2017/12/18.
 */

public class SwipeMenu extends ViewGroup {
    public SwipeMenu(Context context) {
        super(context);
    }

    public SwipeMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScaleTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaxVelocity= ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
    }

    private int expandDuration = 150;
    private int collapseDuration = 150;
    private int mExpandLimit;//展开的阈值
    private int mCollapseLimit;//关闭的阈值
    private float expandRatio = 0.3f;
    private float collapseRatio = 0.7f;

    private int mPointerId;                 //多点触摸只算第一根手指的速度
    private static boolean sIsTouching = false;         //是否已经有手指触碰。防止第二个手指捣乱

    private int mScaleTouchSlop;
    private int mMaxVelocity;               //scrollView抛掷的最大速度
    private VelocityTracker mVelocityTracker; //滑动速度变量

    private int mWidthofMenu;       //菜单布局宽度
    private static SwipeMenu sSwipeMenu;    //静态类写入内存共享。用来判断当前界面是否有menu打开
    private  boolean isExpand = false;       //菜单是否打开
    private boolean isClickEvent = true;    //是否是单击事件

    private PointF mPointGapF = new PointF();//用来设置scrollX，即展开/关闭menu
    private PointF mPointDownF = new PointF();//记录手指第一次点击down的位置

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setClickable(true);
        mWidthofMenu = 0;  //防止多次measure导致累加
        int widthOfContent = 0;//content的宽度
        int heightOfContent = 0;//content的高度
        int childCount = getChildCount();
        for(int i=0;i<childCount;i++){
            View childView = getChildAt(i);
            childView.setClickable(true);
            measureChild(childView,widthMeasureSpec,heightMeasureSpec);
            heightOfContent = Math.max(heightOfContent,childView.getMeasuredHeight());
            if(i==0){
                widthOfContent = getMeasuredWidth();
            }else{
                mWidthofMenu+=childView.getMeasuredWidth();
            }
        }
        mExpandLimit = (int) (mWidthofMenu * expandRatio);
        mCollapseLimit = (int) (mWidthofMenu * collapseRatio);
        setMeasuredDimension(widthOfContent,heightOfContent);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        for(int i=0;i<childCount;i++){
            View childView = this.getChildAt(i);
            if(i==0){
                childView.layout(l,t,l+childView.getMeasuredWidth(),t+childView.getMeasuredHeight());
            }else{
                childView.layout(r,t,r+childView.getMeasuredWidth(),t+childView.getMeasuredHeight());
                r=r+childView.getMeasuredWidth();
            }

        }
    }

    private boolean isInterceptTouch = false;//已经有menu打开，再次点击的时候需要拦截父滑动事件，还要拦截子view事件
    private boolean isInterceptParent = false;//是否拦截了父滑动，即是否要自己要处理滑动

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch(ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(sIsTouching){
                    return false;
                }

                isClickEvent = true;
                isInterceptTouch = false;   //没有menu开启
                isInterceptParent = false;  //不拦截父类滑动

                mPointGapF.x = ev.getX();
                mPointDownF.x = ev.getX();
                mPointDownF.y = ev.getY();
                mPointerId = ev.getPointerId(0);

                if(sSwipeMenu !=null){
                    if(sSwipeMenu!=this){
                        //如果不是当前view 的话自动关闭
                        sSwipeMenu.collapseSmooth();
                        getParent().requestDisallowInterceptTouchEvent(true);
                        isInterceptTouch = true;

                    }

                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(isInterceptTouch){
                    return false;
                }
                isClickEvent = (Math.abs(mPointDownF.x - ev.getX()) < mScaleTouchSlop);//判断是否为点击事件

                float gapX = mPointDownF.x-ev.getX();
                float gapY = mPointDownF.y-ev.getY();
                if(Math.abs(gapX)>mScaleTouchSlop && Math.abs(gapX)>Math.abs(gapY)*2){
                    isInterceptParent = true;
                    sIsTouching = true;
                }
//                else if(Math.abs(gapX)>mScaleTouchSlop|| Math.abs(getScaleX())>mScaleTouchSlop){
//                    isInterceptParent=true;
//                }
                if(!isInterceptParent){
                    break;


                }
                getParent().requestDisallowInterceptTouchEvent(true);
                scrollBy((int)(mPointGapF.x-ev.getX()),0);
                mPointGapF.x = ev.getX();
                if(getScrollX()<0){
                    Log.e("right",getScrollX()+"");
                    scrollTo(0,0);
                    isExpand = false;
                }
                if(getScrollX()>=mWidthofMenu){
                    scrollTo(mWidthofMenu,0);
                    isExpand = true;
                    Log.e("left",isExpand+"");
                }
                break;
            case MotionEvent.ACTION_UP:
                sIsTouching = false;
                if(isInterceptTouch){
                    return  false;
                }

                if(!isExpand){//如果还没有展开
                    if(getScrollX()>mExpandLimit ){
                        expandSmooth();
                    }else{
                        collapseSmooth();
                    }
                }else {//已经展开的
                    if(getScrollX()<mCollapseLimit){
                        collapseSmooth();
                    }else{
                        expandSmooth();
                    }
                }
        }
        return super.dispatchTouchEvent(ev);
    }
//    事件拦截，事件不传递给子view


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(isInterceptTouch ||!isInterceptParent){
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }
//处理长按拖拽事件，展开menu的时候不能长按


    @Override
    public boolean performLongClick() {
        if(sSwipeMenu !=null){
            return false;
        }
        return super.performLongClick();
    }

    private ValueAnimator mExpandAnim,mCollapseAnim;
/*
平滑展开
*/
    public void expandSmooth(){
        sSwipeMenu = SwipeMenu.this;
        cancelAnim();
        mExpandAnim= ValueAnimator.ofInt(getScrollX(),mWidthofMenu);
        mExpandAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                scrollTo((Integer)valueAnimator.getAnimatedValue(),0);
            }
        });
        mExpandAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isExpand = true;
            }
        });
        mExpandAnim.setDuration(expandDuration).start();
    }
/*平滑关闭
* */
    public void collapseSmooth(){
        sSwipeMenu=null;
        cancelAnim();

        mCollapseAnim = ValueAnimator.ofInt(getScrollX(),0);
        mCollapseAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                scrollTo((Integer)valueAnimator.getAnimatedValue(),0);
            }
        });
        mCollapseAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isExpand = false;
            }
        });
        mCollapseAnim.setDuration(collapseDuration).start();
    }
    /*执行动画前都应该取消之前的动画*/
    private void cancelAnim(){
        if(mCollapseAnim !=null && mCollapseAnim.isRunning()){
            mCollapseAnim.cancel();
        }
        if(mExpandAnim !=null && mExpandAnim.isRunning()){
            mExpandAnim.cancel();
        }
    }
}
