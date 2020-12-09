package com.example.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WheelView extends ScrollView {
    public static final String TAG = WheelView.class.getSimpleName();

    private List<String> wheels;
    private Context context;
    private LinearLayout picker;

    public static final int OFFSET_DEFAULT = 1;
    private int offset = OFFSET_DEFAULT;
    private int initialY;
    private int newCheck = 50;
    private int displayItemCount;
    private int selectedIndex = 1;
    private int itemHeight = 0;
    private int selectedItemColor;
    private int unSelectedItemColor;
    private int selectedItemSize;
    private int unSelectedItemSize;
    private int itemSize;
    private int padding;

    private Runnable scrollerTask;

    public WheelView(Context context) {
        super(context);
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.WheelView);
        selectedItemColor = arr.getColor(R.styleable.WheelView_select_color, getResources().getColor(R.color.wheel_select_color, null));
        unSelectedItemColor = arr.getColor(R.styleable.WheelView_unselect_color, getResources().getColor(R.color.wheel_unselect_color, null));
        selectedItemSize = arr.getDimensionPixelSize(R.styleable.WheelView_select_size, getResources().getDimensionPixelSize(R.dimen.wheel_select_size));
        unSelectedItemSize = arr.getDimensionPixelSize(R.styleable.WheelView_unselect_size, getResources().getDimensionPixelSize(R.dimen.wheel_unselect_size));
        itemSize = arr.getDimensionPixelSize(R.styleable.WheelView_itemview_size, getResources().getDimensionPixelSize(R.dimen.wheel_item_height));
        padding = arr.getDimensionPixelOffset(R.styleable.WheelView_itemview_padding, getResources().getDimensionPixelOffset(R.dimen.wheel_item_padding));
        Log.d(TAG, selectedItemColor + ", " + selectedItemSize + ", " + unSelectedItemColor + ", " + unSelectedItemSize + ", " + itemSize + ", " + padding);
        if (null != arr) {
            arr.recycle();
        }
        init(context);
    }

    public void setWheels(List<String> wheels) {
        if (null == wheels){
            wheels = new ArrayList<String>();
        }
        this.wheels = new ArrayList<String>(wheels);

        // 保持第一位和最后一位为空字符串
        for (int i = 0; i < offset; i++){
            this.wheels.add(0, "");
            this.wheels.add("");
        }
        initData();
    }

    private void init(Context context){
        this.context = context;
        this.setVerticalScrollBarEnabled(false);

        picker = new LinearLayout(context);
        picker.setOrientation(LinearLayout.VERTICAL);
        this.addView(picker);

        scrollerTask = new Runnable(){
            public void run(){
                int newY = getScrollY();
                if(initialY - newY == 0){ // Idle
                    final int remainder = initialY % itemHeight;
                    final int divided = initialY / itemHeight;
                    if (remainder == 0) { // stoped at center
                        selectedIndex = divided + offset;
                        onSeletedCallBack(); //
                    } else if (remainder > itemHeight / 2){
                        WheelView.this.post(new Runnable() {
                            @Override
                            public void run() {
                                // 多移动一格
                                WheelView.this.smoothScrollTo(0, initialY - remainder + itemHeight);
                                selectedIndex = divided + offset + 1;
                                onSeletedCallBack();
                            }
                        });
                    } else {
                        WheelView.this.post(new Runnable() {
                            @Override
                            public void run() {
                                WheelView.this.smoothScrollTo(0, initialY - remainder);
                                selectedIndex = divided + offset;
                                onSeletedCallBack();
                            }
                        });
                    }
                } else {
                    initialY = getScrollY();
                    WheelView.this.postDelayed(scrollerTask, newCheck);
                }
            }
        };

        if (null != onWheelViewListener){
            onWheelViewListener.onSelected(selectedIndex, wheels.get(selectedIndex));
        }
    }

    private void initData(){
        //总数目
        displayItemCount = offset * 2 + 1;

        picker.removeAllViews();
        for (String item: wheels){
            picker.addView(createView(item));
        }

        refreshItemView(0);
    }

    // 经反复测算，itemview高度应大于选中时字体大小加上上下边距，笔者用的是50dp高度、30sp大小、20sp大小、5dp边距
    private TextView createView(String wheel){
        TextView mItemView = new TextView(context);
        mItemView.setLayoutParams(new LayoutParams(itemSize,  itemSize));
        mItemView.setSingleLine(true);
        mItemView.setTextSize(TypedValue.COMPLEX_UNIT_SP, unSelectedItemSize);
        if (!wheel.equals("")) {
            mItemView.setText(wheel);
        }
        mItemView.setGravity(Gravity.CENTER);
        mItemView.setPadding(padding, padding, padding, padding);
        if (0 == itemHeight){
            itemHeight = getViewMeasuredHeight(mItemView);
            picker.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, itemHeight * displayItemCount, Gravity.CENTER_HORIZONTAL));
            picker.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.getLayoutParams();
            this.setLayoutParams(new LinearLayout.LayoutParams(lp.width, itemHeight * displayItemCount));
        }
        return mItemView;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        // 滑动后矫正
        refreshItemView(t);
    }

    // 矫正，保持可见数始终为固定数目
    private void refreshItemView(int y){
        int position = y / itemHeight + offset;
        int remainder = y % itemHeight;
        int divided = y / itemHeight;

        if (remainder == 0){
            position = divided + offset;
        } else if (remainder > itemHeight / 2){
            position = divided + offset + 1;
        }

        int childSize = picker.getChildCount();
        for (int i = 0; i < childSize; i++){
            TextView itemView = (TextView) picker.getChildAt(i);
            if (null == itemView){
                return ;
            }

            //刷新时调整itemview样式
            if (position == i){
                itemView.setTextColor(selectedItemColor);
                itemView.setTextSize(TypedValue.COMPLEX_UNIT_SP, selectedItemSize) ;
            } else {
                itemView.setTextColor(unSelectedItemColor);
                itemView.setTextSize(TypedValue.COMPLEX_UNIT_SP, unSelectedItemSize) ;
            }
        }
    }

    /*
        选中回调
     */
    private void onSeletedCallBack(){
        if (null != onWheelViewListener){
            onWheelViewListener.onSelected(selectedIndex, wheels.get(selectedIndex));
        }
    }

    private void startSrollerTask(){
        initialY = getScrollY();
        this.postDelayed(scrollerTask, newCheck);
    }


    @Override
    public void fling(int velocityY) {
        // 降低敏感度
        super.fling(velocityY / 3);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP){
            startSrollerTask();
        }
        return super.onTouchEvent(ev);
    }

    private OnWheelViewListener onWheelViewListener;

    public void setOnWheelViewListener(OnWheelViewListener onWheelViewListener) {
        this.onWheelViewListener = onWheelViewListener;
    }

    public interface OnWheelViewListener {
        public void onSelected(int selectedIndex, String item);
    }

    private int getViewMeasuredHeight(View view){
        int width = View.MeasureSpec.makeMeasureSpec(itemSize, MeasureSpec.EXACTLY);
        int expandSpec = View.MeasureSpec.makeMeasureSpec(itemSize, MeasureSpec.EXACTLY);
        view.measure(width, expandSpec);
        return view.getMeasuredHeight();
    }

    ///// getter and setter  / // //////
    //设置两侧可显示个数
    public void setOffset(int offset) {
        this.offset = offset;
    }


    // 设置初始位置
    public void setSelectedIndex(int selectedIndex) {
        final int p = selectedIndex;
        this.selectedIndex = p + offset;
        this.post(new Runnable() {
            @Override
            public void run() {
                WheelView.this.smoothScrollTo(0, p * itemHeight);
            }
        });
    }

    public void setSelectedValue(String value){
        for (int i = 0; i < wheels.size(); i++){
            String item = wheels.get(i);
            if (item.equals(value)){
                setSelectedIndex(i - offset);
            }
        }
    }
    public  String getSelectedItem(){
        return wheels.get(selectedIndex);
    }

}
