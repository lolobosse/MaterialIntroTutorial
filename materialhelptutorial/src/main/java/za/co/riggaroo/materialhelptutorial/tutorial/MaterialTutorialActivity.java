package za.co.riggaroo.materialhelptutorial.tutorial;

import android.app.ActionBar;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import jobninja.eu.analytics.Analytics;
import za.co.riggaroo.materialhelptutorial.MaterialTutorialFragment;
import za.co.riggaroo.materialhelptutorial.R;
import za.co.riggaroo.materialhelptutorial.TutorialItem;
import za.co.riggaroo.materialhelptutorial.adapter.MaterialTutorialAdapter;
import za.co.riggaroo.materialhelptutorial.view.CirclePageIndicator;


public class MaterialTutorialActivity extends AppCompatActivity implements MaterialTutorialContract.View {

    private static final String TAG = "MaterialTutActivity";
    public static final String MATERIAL_TUTORIAL_ARG_TUTORIAL_ITEMS = "tutorial_items";
    private ViewPager mHelpTutorialViewPager;
    private View mRootView;
    private TextView mTextViewSkip;
    private Button mNextButton;
    private Button mDoneButton;
    private MaterialTutorialPresenter materialTutorialPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_tutorial);

        materialTutorialPresenter = new MaterialTutorialPresenter(this, this);
        setStatusBarColor();
        ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            getActionBar().hide();
        }
        mRootView = findViewById(R.id.activity_help_root);
        mHelpTutorialViewPager = (ViewPager) findViewById(R.id.activity_help_view_pager);
        mTextViewSkip = (TextView) findViewById(R.id.activity_help_skip_textview);
        mNextButton = (Button) findViewById(R.id.activity_next_button);
        mDoneButton = (Button) findViewById(R.id.activity_tutorial_done);

        mTextViewSkip.setOnClickListener(new FinishOrDoneClickListener(this, true));
        mDoneButton.setOnClickListener(new FinishOrDoneClickListener(this, false));


        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialTutorialPresenter.nextClick();


            }
        });
        List<TutorialItem> tutorialItems = getIntent().getParcelableArrayListExtra(MATERIAL_TUTORIAL_ARG_TUTORIAL_ITEMS);
        materialTutorialPresenter.loadViewPagerFragments(tutorialItems);

        // For Analytics only
        mHelpTutorialViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            int previous = 0;
            @Override
            public void onPageSelected(int current) {
                if (previous>current) {
                    Analytics.tutorialPreviousSlideSwiped(MaterialTutorialActivity.this, previous, current);
                }
                else if (current> previous){
                    Analytics.tutorialNextSlideSwiped(MaterialTutorialActivity.this, previous, current);
                }
                previous = current;
            }
        });
    }

    private void setStatusBarColor() {
        if (isFinishing()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    public void showNextTutorial() {
        int currentItem = mHelpTutorialViewPager.getCurrentItem();
        if (currentItem < materialTutorialPresenter.getNumberOfTutorials()) {
            int nextPage = mHelpTutorialViewPager.getCurrentItem() + 1;
            mHelpTutorialViewPager.setCurrentItem(nextPage);
            Analytics.tutorialNextSlideClicked(this, nextPage);
        }
    }

    @Override
    public void showEndTutorial() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void setBackgroundColor(int color) {
        mRootView.setBackgroundColor(color);
    }

    @Override
    public void showDoneButton() {
        mTextViewSkip.setVisibility(View.INVISIBLE);
        mNextButton.setVisibility(View.GONE);
        mDoneButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void showSkipButton() {
        mTextViewSkip.setVisibility(View.VISIBLE);
        mNextButton.setVisibility(View.VISIBLE);
        mDoneButton.setVisibility(View.GONE);
    }

    @Override
    public void setViewPagerFragments(List<MaterialTutorialFragment> materialTutorialFragments) {
        MaterialTutorialAdapter adapter = new MaterialTutorialAdapter(getSupportFragmentManager(), materialTutorialFragments);
        mHelpTutorialViewPager.setAdapter(adapter);
        mHelpTutorialViewPager.setOffscreenPageLimit(adapter.getCount() - 1);
        CirclePageIndicator mCirclePageIndicator = (CirclePageIndicator) findViewById(R.id.activity_help_view_page_indicator);

        mCirclePageIndicator.setViewPager(mHelpTutorialViewPager);
        mCirclePageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                materialTutorialPresenter.onPageSelected(mHelpTutorialViewPager.getCurrentItem());

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        mHelpTutorialViewPager.setPageTransformer(true, new ViewPager.PageTransformer() {
                    @Override
                    public void transformPage(View page, float position) {
                        materialTutorialPresenter.transformPage(page, position);
                    }
                }

        );
    }

    private class FinishOrDoneClickListener implements View.OnClickListener {

        boolean isSkip;
        Context c;

        private FinishOrDoneClickListener(Context c, boolean isSkip) {
            this.isSkip = isSkip;
            this.c = c;
        }

        @Override
        public void onClick(View v) {
            materialTutorialPresenter.doneOrSkipClick();
            Analytics.tutorialEnded(c, isSkip);
        }
    }
}
