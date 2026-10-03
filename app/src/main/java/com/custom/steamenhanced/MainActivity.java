package com.custom.steamenhanced;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebExtension;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SteamEnhanced";

    private static final String HLTB_EXTENSION_URI =
            "resource://android/assets/extensions/hltb/";

    private static final String HLTB_EXTENSION_ID =
            "{6f0e9b42-9e16-4e67-a12b-0d6a5a4c4a10}";

    private GeckoRuntime runtime;
    private GeckoSession session;
    private GeckoView geckoView;

    private String currentUrl;

    /*
     * GeckoView tells us whether there is actual browser
     * history available.
     */
    private boolean canGoBack = false;

    // Native messaging connection to HLTB extension.
    private WebExtension.Port hltbPort;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Window window = getWindow();


        // ---------------------------------------------------------
        // Prefer the highest refresh rate supported by the display.
        // ---------------------------------------------------------

        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.M) {

            /*
             * Use WindowManager instead of Activity.getDisplay()
             * so this also works with minSdk 26.
             */
            Display display =
                    getWindowManager().getDefaultDisplay();

            if (display != null) {

                Display.Mode[] modes =
                        display.getSupportedModes();

                float maxRefreshRate = 0f;

                for (Display.Mode mode : modes) {

                    if (mode.getRefreshRate() >
                            maxRefreshRate) {

                        maxRefreshRate =
                                mode.getRefreshRate();
                    }
                }

                if (maxRefreshRate > 0f) {

                    WindowManager.LayoutParams params =
                            window.getAttributes();

                    params.preferredRefreshRate =
                            maxRefreshRate;

                    window.setAttributes(params);

                    Log.i(
                            TAG,
                            "Preferred refresh rate: "
                                    + maxRefreshRate
                                    + " Hz"
                    );
                }
            }
        }


        // ---------------------------------------------------------
        // Black system bars.
        // ---------------------------------------------------------

        window.setStatusBarColor(Color.BLACK);
        window.setNavigationBarColor(Color.BLACK);
        window.getDecorView().setSystemUiVisibility(0);


        setContentView(R.layout.activity_main);


        geckoView =
                findViewById(R.id.geckoview);


        runtime =
                GeckoRuntime.create(this);


        session =
                new GeckoSession();


        geckoView.setSession(session);


        // ---------------------------------------------------------
        // Android Back button.
        //
        // GeckoView itself determines whether browser history
        // exists. This works for Steam, HLTB and any other page.
        // ---------------------------------------------------------

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (canGoBack) {

                            Log.i(
                                    TAG,
                                    "Going back in GeckoView history"
                            );

                            session.goBack();

                        } else {

                            Log.i(
                                    TAG,
                                    "No GeckoView history -> closing Activity"
                            );

                            /*
                             * Disable this callback so the normal
                             * Android Back behavior can happen.
                             */
                            setEnabled(false);

                            getOnBackPressedDispatcher()
                                    .onBackPressed();
                        }
                    }
                }
        );


        // ---------------------------------------------------------
        // GeckoView Navigation Delegate.
        // ---------------------------------------------------------

        session.setNavigationDelegate(
                new GeckoSession.NavigationDelegate() {

                    @Override
                    public void onLocationChange(
                            GeckoSession session,
                            String url,
                            java.util.List<
                                    GeckoSession.PermissionDelegate.ContentPermission
                                    > perms,
                            Boolean hasUserGesture) {

                        currentUrl = url;

                        Log.i(
                                TAG,
                                "URL changed: " + url
                        );


                        if (url != null &&
                                url.contains(
                                        "store.steampowered.com/app/")) {

                            Log.i(
                                    TAG,
                                    "Steam game page detected"
                            );
                        }
                    }


                    /*
                     * GeckoView calls this whenever its actual
                     * navigation history changes.
                     *
                     * This is what the Android Back button uses.
                     */
                    @Override
                    public void onCanGoBack(
                            GeckoSession session,
                            boolean canGoBack) {

                        MainActivity.this.canGoBack =
                                canGoBack;

                        Log.i(
                                TAG,
                                "Can go back: " + canGoBack
                        );
                    }


                    /*
                     * Handle links that GeckoView wants to open
                     * in a new browsing session.
                     *
                     * We keep them inside our existing
                     * GeckoSession.
                     */
                    @Override
                    public GeckoResult<GeckoSession> onNewSession(
                            GeckoSession session,
                            String uri) {

                        Log.i(
                                TAG,
                                "New session requested: " + uri
                        );


                        /*
                         * Load the requested URL into our existing
                         * GeckoSession.
                         */
                        session.loadUri(uri);


                        /*
                         * Tell GeckoView that we handled the
                         * new session ourselves.
                         */
                        return GeckoResult.fromValue(null);
                    }
                }
        );


        session.open(runtime);


        installAugmentedSteam();
    }


    // =============================================================
    // AUGMENTED STEAM
    // =============================================================

    private void installAugmentedSteam() {

        Log.i(
                TAG,
                "Installing Augmented Steam..."
        );


        runtime.getWebExtensionController()
                .ensureBuiltIn(
                        "resource://android/assets/extensions/augmented_steam/",
                        "{1be309c5-3e4f-4b99-927d-bb500eb4fa88}"
                )
                .accept(

                        extension -> {

                            Log.i(
                                    TAG,
                                    "Augmented Steam installed successfully"
                            );

                            installHLTBExtension();
                        },

                        error -> {

                            Log.e(
                                    TAG,
                                    "Failed to install Augmented Steam",
                                    error
                            );

                            /*
                             * Even if Augmented Steam fails,
                             * still install HLTB.
                             */
                            installHLTBExtension();
                        }
                );
    }


    // =============================================================
    // HLTB EXTENSION
    // =============================================================

    private void installHLTBExtension() {

        Log.i(
                TAG,
                "Installing HLTB extension..."
        );


        runtime.getWebExtensionController()
                .ensureBuiltIn(
                        HLTB_EXTENSION_URI,
                        HLTB_EXTENSION_ID
                )
                .accept(

                        extension -> {

                            Log.i(
                                    TAG,
                                    "HLTB extension installed successfully"
                            );


                            setupHLTBMessageDelegate(
                                    extension
                            );


                            /*
                             * Only load Steam after both
                             * extensions are ready.
                             */
                            session.loadUri(
                                    "https://store.steampowered.com/"
                            );
                        },

                        error -> {

                            Log.e(
                                    TAG,
                                    "Failed to install HLTB extension",
                                    error
                            );


                            /*
                             * Still load Steam if HLTB installation
                             * fails.
                             */
                            session.loadUri(
                                    "https://store.steampowered.com/"
                            );
                        }
                );
    }


    // =============================================================
    // HLTB NATIVE MESSAGING
    // =============================================================

    /**
     * Set up bidirectional native messaging.
     *
     * content.js:
     *
     *     runtime.connectNative("browser")
     *
     *              ↓
     *
     * Java:
     *
     *     onConnect()
     *
     *              ↓
     *
     * HLTBManager
     *
     *              ↓
     *
     * port.postMessage()
     *
     *              ↓
     *
     * content.js:
     *
     *     port.onMessage
     */
    private void setupHLTBMessageDelegate(
            WebExtension extension) {

        WebExtension.MessageDelegate messageDelegate =
                new WebExtension.MessageDelegate() {


                    @Override
                    public void onConnect(
                            WebExtension.Port port) {

                        Log.i(
                                TAG,
                                "HLTB native connection established"
                        );


                        hltbPort = port;


                        Log.i(
                                TAG,
                                "HLTB port name: "
                                        + port.name
                        );


                        port.setDelegate(
                                new WebExtension.PortDelegate() {

                                    @Override
                                    public void onPortMessage(
                                            Object message,
                                            WebExtension.Port port) {

                                        Log.i(
                                                TAG,
                                                "HLTB port message received"
                                        );


                                        if (!(message instanceof JSONObject)) {

                                            Log.e(
                                                    TAG,
                                                    "Invalid HLTB port message"
                                            );

                                            return;
                                        }


                                        JSONObject json =
                                                (JSONObject) message;


                                        try {

                                            String type =
                                                    json.optString(
                                                            "type",
                                                            ""
                                                    );


                                            if (!"GET_HLTB".equals(type)) {

                                                Log.e(
                                                        TAG,
                                                        "Unknown HLTB message type: "
                                                                + type
                                                );

                                                return;
                                            }


                                            String gameName =
                                                    json.optString(
                                                            "gameName",
                                                            ""
                                                    );


                                            if (gameName.isEmpty()) {

                                                sendHLTBError(
                                                        port,
                                                        "Game name missing"
                                                );

                                                return;
                                            }


                                            Log.i(
                                                    TAG,
                                                    "HLTB lookup requested: "
                                                            + gameName
                                            );


                                            HLTBManager.lookup(
                                                    gameName,
                                                    new HLTBManager.Callback() {

                                                        @Override
                                                        public void onSuccess(
                                                                String resultGameName,
                                                                double hours,
                                                                String url) {

                                                            try {

                                                                JSONObject response =
                                                                        new JSONObject();


                                                                response.put(
                                                                        "success",
                                                                        true
                                                                );


                                                                response.put(
                                                                        "found",
                                                                        true
                                                                );


                                                                response.put(
                                                                        "gameName",
                                                                        resultGameName
                                                                );


                                                                response.put(
                                                                        "hours",
                                                                        hours
                                                                );


                                                                response.put(
                                                                        "url",
                                                                        url
                                                                );


                                                                Log.i(
                                                                        TAG,
                                                                        "HLTB result: "
                                                                                + resultGameName
                                                                                + " = "
                                                                                + hours
                                                                                + " hours"
                                                                );


                                                                port.postMessage(
                                                                        response
                                                                );


                                                                Log.i(
                                                                        TAG,
                                                                        "HLTB response sent to extension"
                                                                );


                                                            } catch (Exception e) {

                                                                Log.e(
                                                                        TAG,
                                                                        "Failed to send HLTB result",
                                                                        e
                                                                );


                                                                sendHLTBError(
                                                                        port,
                                                                        e.getMessage()
                                                                );
                                                            }
                                                        }


                                                        @Override
                                                        public void onError(
                                                                String error) {

                                                            Log.e(
                                                                    TAG,
                                                                    "HLTB lookup failed: "
                                                                            + error
                                                            );


                                                            sendHLTBError(
                                                                    port,
                                                                    error
                                                            );
                                                        }
                                                    }
                                            );


                                        } catch (Exception e) {

                                            Log.e(
                                                    TAG,
                                                    "Error processing HLTB port message",
                                                    e
                                            );


                                            sendHLTBError(
                                                    port,
                                                    e.getMessage()
                                            );
                                        }
                                    }


                                    @Override
                                    public void onDisconnect(
                                            WebExtension.Port port) {

                                        Log.i(
                                                TAG,
                                                "HLTB native connection disconnected"
                                        );


                                        if (hltbPort == port) {

                                            hltbPort = null;
                                        }
                                    }
                                }
                        );
                    }
                };


        /*
         * Register the delegate for messages coming from
         * content scripts.
         */
        session.getWebExtensionController()
                .setMessageDelegate(
                        extension,
                        messageDelegate,
                        "browser"
                );


        Log.i(
                TAG,
                "HLTB native messaging delegate registered"
        );
    }


    // =============================================================
    // HLTB ERROR / NOT FOUND RESPONSE
    // =============================================================

    /**
     * Send a response when HLTB has no usable data.
     *
     * success=true means the native messaging request
     * itself succeeded.
     *
     * found=false tells content.js that the game simply
     * has no usable HLTB data.
     */
    private void sendHLTBError(
            WebExtension.Port port,
            String error) {

        try {

            JSONObject response =
                    new JSONObject();


            response.put(
                    "success",
                    true
            );


            response.put(
                    "found",
                    false
            );


            response.put(
                    "hours",
                    JSONObject.NULL
            );


            response.put(
                    "url",
                    ""
            );


            response.put(
                    "error",
                    error != null
                            ? error
                            : "HLTB data not found"
            );


            port.postMessage(
                    response
            );


            Log.i(
                    TAG,
                    "HLTB not-found response sent"
            );


        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Failed to send HLTB not-found response",
                    e
            );
        }
    }


    // =============================================================
    // CLEANUP
    // =============================================================

    @Override
    protected void onDestroy() {

        if (hltbPort != null) {

            try {

                hltbPort.disconnect();

            } catch (Exception ignored) {
            }

            hltbPort = null;
        }


        if (session != null) {

            session.close();
        }


        super.onDestroy();
    }
}