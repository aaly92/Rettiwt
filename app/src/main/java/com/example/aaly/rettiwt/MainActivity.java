package com.example.aaly.rettiwt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aaly.rettiwt.data.remote.ConnectionDetector;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends AppCompatActivity {

    private static final String TWITTER_CONSUMER_KEY = "GlDm2ZWJpW0hT3sWJTf1h53Jf";
    private static final String TWITTER_CONSUMER_SECRET = "9mx1VF2dBym190StEPdJa2B7X49fwThe1JmbVWjonI9mLrkCpl";

    private static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    private static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    private static final String PREF_KEY_USER_ID = "user_id";
    private static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    private static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
    private static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";

    private static final String ALERT_NO_INTERNET_CONNECTION_MESSAGE = "Please connect to the Internet";
    private static final String SHARED_PREF_KEY = "MyPref";

    private static final String TWITTER_LOGIN_ERROR = "Twitter Login Error";


    private Twitter twitter;
    private RequestToken requestToken;
    private SharedPreferences sharedPreferences;
    private ConnectionDetector connectionDetector;

    @BindView(R.id.username_label)
    TextView usernameLabel;
    @BindView(R.id.twitter_login_btn)
    Button twitterLoginBtn;
    @BindView(R.id.twitter_logout_btn)
    Button twitterLogoutBtn;

    @OnClick(R.id.twitter_login_btn)
    public void login() {
        loginToTwitter();
    }

    @OnClick(R.id.twitter_logout_btn)
    public void logout() {
        logoutFromTwitter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREF_KEY, 0);
        if (!isConnectedToInternet()) {
            return;
        }
        if (!isLoggedIn()) {
            Uri uri = getIntent().getData();
            if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
                fetchOauthKeysFromRedirect(uri);
            }
        } else {
            twitterLogoutBtn.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getTwitterFeed();
    }

    private boolean isConnectedToInternet() {
        connectionDetector = new ConnectionDetector(getApplicationContext());
        if (!connectionDetector.isConnectingToInternet()) {
            Toast.makeText(this, ALERT_NO_INTERNET_CONNECTION_MESSAGE, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }

    private void loginToTwitter() {
        if (!isLoggedIn()) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ConfigurationBuilder builder = new ConfigurationBuilder();
                        builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
                        builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
                        Configuration configuration = builder.build();

                        TwitterFactory factory = new TwitterFactory(configuration);
                        twitter = factory.getInstance();
                        requestToken = twitter
                                .getOAuthRequestToken(TWITTER_CALLBACK_URL);
                        MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                                .parse(requestToken.getAuthenticationURL())));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
        getTwitterFeed();
    }

    private void fetchOauthKeysFromRedirect(Uri uri) {
        final Uri urlFinal = uri;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String verifier = urlFinal.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
                    AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
                    SharedPreferences.Editor e = sharedPreferences.edit();
                    e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
                    e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
                    e.putString(PREF_KEY_USER_ID, Long.toString(accessToken.getUserId()));
                    e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
                    e.commit();
                    getTwitterFeed();
                } catch (Exception e) {
                    Log.e(TWITTER_LOGIN_ERROR, "> " + e.getMessage());
                }
            }
        });
        thread.start();
    }

    private void logoutFromTwitter() {
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.remove(PREF_KEY_OAUTH_TOKEN);
        e.remove(PREF_KEY_OAUTH_SECRET);
        e.remove(PREF_KEY_TWITTER_LOGIN);
        e.commit();

        twitterLogoutBtn.setVisibility(View.GONE);
        usernameLabel.setText("");
        usernameLabel.setVisibility(View.GONE);

        twitterLoginBtn.setVisibility(View.VISIBLE);
    }

    private void getTwitterFeed() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isLoggedIn()) {
                        ConfigurationBuilder builder = new ConfigurationBuilder();
                        Configuration configuration = builder
                                .setOAuthConsumerKey(TWITTER_CONSUMER_KEY)
                                .setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET)
                                .setOAuthAccessToken(sharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, null))
                                .setOAuthAccessTokenSecret(sharedPreferences.getString(PREF_KEY_OAUTH_SECRET, null))
                                .build();

                        TwitterFactory factory = new TwitterFactory(configuration);
                        twitter = factory.getInstance();
                        long userID = Long.parseLong(sharedPreferences.getString(PREF_KEY_USER_ID, ""));
                        final User user = twitter.showUser(userID);
                        ResponseList<Status> timeLine = twitter.getUserTimeline(userID);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                twitterLoginBtn.setVisibility(View.GONE);
                                usernameLabel.setVisibility(View.VISIBLE);
                                twitterLogoutBtn.setVisibility(View.VISIBLE);
                                if (user != null) {
                                    usernameLabel.setText(Html.fromHtml("<b>Welcome " + user.getName() + "</b>"));
                                }
                                twitterLogoutBtn.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                twitterLoginBtn.setVisibility(View.VISIBLE);
                                usernameLabel.setVisibility(View.GONE);
                                twitterLogoutBtn.setVisibility(View.GONE);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TWITTER_LOGIN_ERROR, "> " + e.getMessage());
                }
            }
        });
        thread.start();
    }

}
