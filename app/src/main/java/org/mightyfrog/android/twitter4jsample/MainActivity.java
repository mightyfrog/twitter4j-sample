package org.mightyfrog.android.twitter4jsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author Shigehiro Soejima
 */
public class MainActivity extends AppCompatActivity implements Const {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_OAUTH = 1 + 0xDEAD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startActivityForResult(new Intent(this, OAuthActivity.class), REQUEST_CODE_OAUTH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_OAUTH: {
                if (resultCode == RESULT_OK) {
                    AccessToken token = (AccessToken) data.getSerializableExtra(ACCESS_TOKEN);
                    android.util.Log.e(TAG, token.getToken());
                    android.util.Log.e(TAG, token.getTokenSecret());
                    searchTest(token);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /* PRIVATE METHODS */

    /**
     * @param accessToken The access token.
     */
    private void searchTest(AccessToken accessToken) {
        if (accessToken == null) {
            return;
        }

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(CONSUMER_KEY);
        builder.setOAuthConsumerSecret(CONSUMER_SECRET);
        builder.setOAuthAccessToken(accessToken.getToken());
        builder.setOAuthAccessTokenSecret(accessToken.getTokenSecret());
        Configuration configuration = builder.build();
        final Twitter twitter = new TwitterFactory(configuration).getInstance();

        Single.create(new SingleOnSubscribe<QueryResult>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<QueryResult> emitter) throws Exception {
                Query query = new Query("Lebowski");
                try {
                    emitter.onSuccess(twitter.search(query));
                } catch (TwitterException e) {
                    emitter.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<QueryResult>() {
                    @Override
                    public void onSuccess(@NonNull QueryResult result) {
                        StringBuilder sb = new StringBuilder();
                        for (Status status : result.getTweets()) {
                            sb.append(status.toString()).append("\n\n");
                        }
                        ((TextView) findViewById(R.id.textView)).setText(sb.toString());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        android.util.Log.e(TAG, "" + e);
                    }
                });
    }
}
