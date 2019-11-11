package com.game;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.GET_ACCOUNTS;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.game.MonsterSokoban.R;
import com.game.layers.MainLayer;
import com.game.logger.Logger;
import com.game.preferences.DevicePreferences;
import com.game.preferences.GlobalPreferences;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesClient;
import java.util.ArrayList;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.opengl.CCGLSurfaceView;
import org.cocos2d.sound.SoundEngine;

/**
 * Class for handling application life cycle and graphics engine.
 *
 * @author niewinskip
 */
@SuppressLint("NewApi")
public class MainActivity extends Activity {

  /**
   * Gingerbread build version.
   */
  private static final int ANDROID_BUILD_GINGERBREAD = 9;
  private static final int PERMISSIONS_REQUEST_CODE = 1240;
  /**
   * Google API
   */
  private static final int RC_SIGN_IN = 9001;
  public static MainActivity INSTANCE;
  /**
   * CCGLSurfaceView view.
   */
  private static CCGLSurfaceView glSurfaceView = null;
  /**
   * Only for the first execution of the onStart() method.
   */
  private static volatile boolean isFirstRunFlag = false;
  private boolean touchOn = true;

  public static MainActivity getINSTANCE() {
    return INSTANCE;
  }

  public static void signInGoogleApi() {
    // Google API
    GoogleSignInClient signInClient = GoogleSignIn.getClient(getINSTANCE(), DEFAULT_GAMES_SIGN_IN);
    getINSTANCE().startActivityForResult(signInClient.getSignInIntent(), RC_SIGN_IN);
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  @Override
  protected final void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    setContentView(R.layout.activity_main);
    Logger.log("onCreate");

    INSTANCE = this;

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    /**
     * Set some flags to ensure full screen. Attach the OpenGL surface to
     * the current application screen. Hide android toolbar.
     */
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    /**
     * Set up the OpenGL surface for Cocos2D.
     */
    if (glSurfaceView == null) {
      glSurfaceView = new CCGLSurfaceView(this);
      setContentView(glSurfaceView);
      glSurfaceView.setPreserveEGLContextOnPause(true);
      glSurfaceView.bringToFront();
    }

    this.touchOn = true;

    /**
     * Allow screen rotation but keep it horizontal. Only works for SDK
     * versions better than Gingerbread (9.0).
     */
    if (Build.VERSION.SDK_INT >= ANDROID_BUILD_GINGERBREAD) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }
    /**
     * Check for permissions, new API.
     */
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      createPermissions();
    }

    /**
     * Allow buttons to regulate music volume. Don't override. Works in 99%
     * media.
     */
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    /**
     * Set true only if onCreate finished.
     */
    isFirstRunFlag = true;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RC_SIGN_IN) {
      Logger.log("onActivityResult for SignIn");
      GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
      if (result.isSuccess()) {
        Logger.log("onActivityResult successfully signed in");
        // The signed in account is stored in the result.
        GoogleSignInAccount signedInAccount = result.getSignInAccount();
        GamesClient gamesClient = Games.getGamesClient(getINSTANCE(), signedInAccount);
        gamesClient.setViewForPopups(getWindow().getDecorView().findViewById(android.R.id.content));
//        gamesClient.setViewForPopups(findViewById(R.id.activity_main));
      } else {
        String message = result.getStatus().getStatusMessage();
        if (message == null || message.isEmpty()) {
          message = getString(R.string.signin_other_error);
        }
        new AlertDialog.Builder(this).setMessage(message)
            .setNeutralButton(android.R.string.ok, null).show();
      }
    }
  }

  public boolean createPermissions() {
    String[] permissions = new String[]{
        INTERNET,
        ACCESS_NETWORK_STATE, MODIFY_AUDIO_SETTINGS, GET_ACCOUNTS};
    ArrayList<String> permissionsNeeded = new ArrayList<>();
    for (String permission : permissions) {
      if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
        permissionsNeeded.add(permission);
      }
    }
    // Ask for missing permissions
    if (!permissionsNeeded.isEmpty()) {
      ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), PERMISSIONS_REQUEST_CODE);
      return false;
    }
    return true;
  }

  @Override
  public final void onStart() {
    super.onStart();
    Logger.log("On start");
    /**
     * Tell Cocos2D which surface to render, set the screen orientation to
     * landscape left, to display the current FPS rate and set animation
     * interval to 60fps. The actual frame rate achieved depends on the
     * capabilities of the device.
     */
    CCDirector.sharedDirector().attachInView(glSurfaceView);
    CCDirector.sharedDirector().setDisplayFPS(false);
    CCDirector.sharedDirector().setAnimationInterval(1.0f / 60.0f);
    if (isFirstRunFlag) {
      CCScene scene = MainLayer.scene();
      scene.setTag(GlobalPreferences.MAIN_LAYER_TAG);
      CCDirector.sharedDirector().runWithScene(scene);
    }

    signInGoogleApi();
  }

  @Override
  public final void onPause() {
    super.onPause();
    Logger.log("On pause");
    CCDirector.sharedDirector().pause();
    glSurfaceView.onPause();
    SoundEngine.sharedEngine().pauseSound();
  }

  @Override
  public final void onResume() {
    super.onResume();
    Logger.log("On resume");
    CCDirector.sharedDirector().resume();
    glSurfaceView.onResume();
    if (DevicePreferences.SOUND_MODE >= 2) {
      SoundEngine.sharedEngine().resumeSound();
    }
  }

  @Override
  public final void onStop() {
    super.onStop();
    Logger.log("On stop");
  }

  @Override
  public final void onWindowFocusChanged(final boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    Logger.log("On focus changed: " + hasFocus);
  }

  @Override
  public final void onDestroy() {
    super.onDestroy();
    Logger.log("On destroy");
  }

  /**
   * Handle back button events. If touch is on, than trigger event from
   * currently running layer. In other case bring openGL ES and whole
   * application to background.
   *
   * Each time back button event is fired, it will be disabled for 1.2 second
   * duration in new thread.
   */
  @Override
  public final boolean onKeyDown(final int keyCode, final KeyEvent event) {
    Logger.log("Key on keypad pressed");
    boolean keyTriggered = true;
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      Logger.log("Back key pressed. Touch is " + touchOn);
      if (this.touchOn) {
        setTouch(false);
        int sceneTag = CCDirector.sharedDirector().getRunningScene().getTag();
        Logger.log("Switching from layer with tag: " + sceneTag);
        if (sceneTag == GlobalPreferences.MAIN_LAYER_TAG) {
          setTouch(true);
          Logger.log("No more layers on stack, closing Activity");
          keyTriggered = false;
          this.moveTaskToBack(true);
        } else {
          if (sceneTag == -1) {
            setTouch(true);
            keyTriggered = false;
            Logger.log("Scene not found.");
          } else {
            Logger.log("Passing event to current layer");
            // Issue #85, #86
            return CCDirector.sharedDirector().onKeyDown(event);
          }
        }
      } else {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            setTouch(true);
          }
        }, 1200);
        return false;
      }
    }
    if (keyTriggered) {
      return super.onKeyDown(keyCode, event);
    } else {
      return false;
    }
  }

  /**
   * Set touch for Activity.
   *
   * @param set Set touchOn.
   */
  public final void setTouch(final boolean set) {
    this.touchOn = set;
  }
}
