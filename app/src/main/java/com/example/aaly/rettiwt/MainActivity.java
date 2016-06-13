package com.example.aaly.rettiwt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.example.aaly.rettiwt.data.remote.TwitterAPI;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TWITTER_CONSUMER_KEY = "GlDm2ZWJpW0hT3sWJTf1h53Jf";
    private static final String TWITTER_CONSUMER_SECRET = "9mx1VF2dBym190StEPdJa2B7X49fwThe1JmbVWjonI9mLrkCpl";
    private static final TwitterAPI twitterApi = TwitterAPI.Factory.getInstance();

    @BindView(R.id.oauth_login)
    Button oauthLogin;

    @OnClick(R.id.oauth_login)
    public void launchLoginPage() {
        //launch outh login
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }
}
