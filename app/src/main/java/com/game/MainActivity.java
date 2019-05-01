package com.game;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.GET_ACCOUNTS;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import com.game.helpers.InGameHelper;
import com.game.layers.MainLayer;
import com.game.logger.Logger;
import com.game.preferences.DevicePreferences;
import com.game.preferences.GlobalPreferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.games.Games;
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
public class MainActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {

  /**
   * Tag for Google API resolution handler. Called when there were problems
   * or required actions regarding connecting with Google Services.
   */
  private static final int GOOGLE_API_RESOLUTION_CODE = InGameHelper.generateUniqueTag();
  /**
   * Gingerbread build version.
   */
  private static final int ANDROID_BUILD_GINGERBREAD = 9;
  private static final int PERMISSIONS_REQUEST_CODE = 1240;
  /**
   * CCGLSurfaceView view.
   */
  private static CCGLSurfaceView glSurfaceView = null;
  /**
   * Only for the first execution of the onStart() method.
   */
  private static volatile boolean onStartFirstFlag = false;
  private boolean touchOn = true;

  /**
   * Determine if user is connected to Google Play.
   *
   * @return True if connected, false if not.
   */
  public static boolean isSignedIn() {
    if (DevicePreferences.getGoogleApiClient() == null) {
      Logger.log("GoogleApi is null");
      return false;
    } else if (!DevicePreferences.getGoogleApiClient().isConnected()) {
      Logger.log("GoogleAPi not connected");
      return false;
    }
    return true;
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  @Override
  protected final void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Logger.log("On create");

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    /**
     * Set some flags to ensure full screen. Attach the OpenGL surface to
     * the current application screen. Hide android toolbar.
     */
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		Due to issue: #115 - On screen buttons were still visible. deviShould be not visible.
//		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//			View decorView = getWindow().getDecorView();
//			int UIOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
//			decorView.setSystemUiVisibility(UIOptions);
//		setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
//				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
    onStartFirstFlag = true;
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
    if (onStartFirstFlag) {
      CCScene scene = MainLayer.scene();
      scene.setTag(GlobalPreferences.MAIN_LAYER_TAG);
      CCDirector.sharedDirector().runWithScene(scene);
    }

    /**
     * Start Google Play Services in new Thread.
     */
    if (onStartFirstFlag) {
      onStartFirstFlag = false;
      Thread connect = new Thread() {
        public void run() {
          Logger.log("Thread started");
          startGoogleServices();
          DevicePreferences.getGoogleApiClient().connect();
          Logger.log("Thread stopped");
        }
      };
      connect.start();
    } else {
      if (isSignedIn()) {
        DevicePreferences.getGoogleApiClient().connect();
      }
    }
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
    if (DevicePreferences.getGoogleApiClient() == null) {
      startGoogleServices();
    }
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
  // finish();
  // CCDirector.sharedDirector().end();

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
   * Create Google Services Client.
   */
  private synchronized void startGoogleServices() {
    GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this).addApi(Games.API).build();
    DevicePreferences.setGoogleApiClient(mGoogleApiClient);
    Logger.log("Started Google Services");
  }

  @Override
  public void onConnected(final Bundle arg0) {
    Logger.log("On connected");
    DevicePreferences.getGoogleApiClient().connect();
  }

  /**
   * Attempt to reconnect.
   */
  @Override
  public void onConnectionSuspended(final int cause) {
    Logger.log("On connection suspended");
    DevicePreferences.getGoogleApiClient().reconnect();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void onConnectionFailed(final ConnectionResult result) {
    Logger.log("On connection failed with error code (" + result.getErrorCode() + ")[" + result.toString() + "]");
    if (!result.hasResolution()) {
      Logger.log("No resolution found");
      GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
      return;
    }
    try {
      Logger.log("Resolution found");
      result.startResolutionForResult(this, GOOGLE_API_RESOLUTION_CODE);
    } catch (SendIntentException e) {
      Logger.log("Exception while starting resolution activity", e.getMessage());
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
