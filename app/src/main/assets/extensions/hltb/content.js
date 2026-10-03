(() => {

    console.log("[SteamEnhanced HLTB] Loaded");

    let nativePort = null;


    function getGameName() {

        const element =
            document.querySelector(".apphub_AppName");

        if (!element) {

            console.log(
                "[SteamEnhanced HLTB] Game name not found"
            );

            return null;
        }

        return element.textContent.trim();
    }


    function connectNative() {

        try {

            console.log(
                "[SteamEnhanced HLTB] Connecting to native app..."
            );


            nativePort =
                browser.runtime.connectNative(
                    "browser"
                );


            console.log(
                "[SteamEnhanced HLTB] Native connection established"
            );


            nativePort.onMessage.addListener(
                result => {

                    console.log(
                        "[SteamEnhanced HLTB] Java response:",
                        result
                    );


                    /*
                     * Java sends:
                     *
                     * found: true
                     *   -> HLTB data exists
                     *
                     * found: false
                     *   -> game not found OR no usable
                     *      main-story data
                     *
                     * Always display a box.
                     */


                    if (!result) {

                        displayResult({
                            hours: null,
                            url: null
                        });

                        return;
                    }


                    /*
                     * Explicit HLTB "not found".
                     */
                    if (result.found === false) {

                        displayResult({
                            hours: null,
                            url: null
                        });

                        return;
                    }


                    /*
                     * Generic failure fallback.
                     */
                    if (!result.success) {

                        displayResult({
                            hours: null,
                            url: null
                        });

                        return;
                    }


                    /*
                     * Valid result.
                     */
                    displayResult(result);
                }
            );


            nativePort.onDisconnect.addListener(
                () => {

                    console.log(
                        "[SteamEnhanced HLTB] Native connection disconnected"
                    );

                    nativePort = null;
                }
            );


        } catch (error) {

            console.error(
                "[SteamEnhanced HLTB] Native connection error:",
                error
            );
        }
    }


    function requestHLTB() {

        const gameName =
            getGameName();


        if (!gameName) {
            return;
        }


        if (!nativePort) {

            console.log(
                "[SteamEnhanced HLTB] Native port unavailable"
            );

            return;
        }


        console.log(
            "[SteamEnhanced HLTB] Requesting HLTB for:",
            gameName
        );


        nativePort.postMessage({

            type: "GET_HLTB",

            gameName: gameName

        });
    }


    function displayResult(result) {

        /*
         * Prevent duplicate boxes.
         */
        if (
            document.getElementById(
                "steam-enhanced-hltb"
            )
        ) {
            return;
        }


        /*
         * Find Steam's TAGS section.
         */
        const tags =
            document.querySelector(
                ".glance_tags_ctn"
            );


        if (!tags) {

            console.log(
                "[SteamEnhanced HLTB] Tags section not found"
            );

            /*
             * Steam may still be rendering.
             */
            setTimeout(
                () => displayResult(result),
                1000
            );

            return;
        }


        /*
         * Determine whether HLTB data is valid.
         */
        const validHours =
            typeof result.hours === "number" &&
            isFinite(result.hours) &&
            result.hours > 0;


        /*
         * Create the box.
         *
         * <a> is used so valid HLTB results
         * can be clicked.
         */
        const box =
            document.createElement("a");


        box.id =
            "steam-enhanced-hltb";


        /*
         * Valid HLTB result:
         * make the box clickable.
         */
        if (
            validHours &&
            result.url
        ) {

            box.href =
                result.url;

            box.target =
                "_blank";

            box.rel =
                "noopener noreferrer";
        }


        /*
         * Display text.
         */
        if (validHours) {

            box.textContent =
                result.hours + " Hours";

        } else {

            box.textContent =
                "Not Found";

            /*
             * Not Found must NOT be clickable.
             */
            box.removeAttribute("href");
            box.removeAttribute("target");
            box.removeAttribute("rel");

            box.style.cursor =
                "default";
        }


        /*
         * Neon-style appearance.
         */
        box.style.display =
            "block";

        box.style.width =
            "fit-content";

        box.style.marginTop =
            "10px";

        box.style.padding =
            "7px 14px";

        box.style.background =
            "#0b0f14";

        box.style.border =
            "1px solid #00ffff";

        box.style.borderRadius =
            "4px";

        box.style.color =
            "#00ffff";

        box.style.fontSize =
            "14px";

        box.style.fontWeight =
            "bold";

        box.style.textDecoration =
            "none";

        box.style.boxShadow =
            "0 0 5px #00ffff, " +
            "0 0 10px rgba(0,255,255,0.6), " +
            "inset 0 0 6px rgba(0,255,255,0.15)";


        /*
         * Hover effect.
         *
         * Only valid HLTB results get
         * the clickable hover appearance.
         */
        if (validHours) {

            box.addEventListener(
                "mouseenter",
                () => {

                    box.style.background =
                        "#00191c";

                    box.style.boxShadow =
                        "0 0 8px #00ffff, " +
                        "0 0 18px #00ffff, " +
                        "inset 0 0 8px rgba(0,255,255,0.25)";
                }
            );


            box.addEventListener(
                "mouseleave",
                () => {

                    box.style.background =
                        "#0b0f14";

                    box.style.boxShadow =
                        "0 0 5px #00ffff, " +
                        "0 0 10px rgba(0,255,255,0.6), " +
                        "inset 0 0 6px rgba(0,255,255,0.15)";
                }
            );
        }


        /*
         * Insert immediately after TAGS.
         */
        tags.parentNode.insertBefore(
            box,
            tags.nextSibling
        );


        console.log(
            "[SteamEnhanced HLTB] Result displayed:",
            box.textContent
        );
    }


    /*
     * Connect to the Android native app.
     */
    connectNative();


    /*
     * Give Steam time to finish rendering,
     * then request HLTB.
     */
    setTimeout(
        requestHLTB,
        1000
    );


})();