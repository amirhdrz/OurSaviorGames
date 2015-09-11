<!DOCTYPE html>
<html>
<head>
	<link rel="alternate" href="android-app://com.oursaviorgames.android/http/oursaviorgames.com/play?g=${gameIdBase64}" />
</head>

<body>
  <!-- iframe used for attempting to load a custom protocol -->
  <iframe style="display:none" height="0" width="0" id="loader"></iframe>

  <script>(function(){

	// Load our custom protocol in the iframe, for Chrome and Opera this burys the error dialog (which is actually HTML)
	// for iOS we will get a popup error if this protocol is not supported, but it won't block javascript
	document.getElementById('loader').src = 'android-app://com.oursaviorgames.android/http/oursaviorgames.com/play?g=${gameIdBase64}';

	// The fallback link for Android needs to be https:// rather than market:// or the device will try to 
	// load both URLs and only the last one will win. (Especially FireFox, where an "Are You Sure" dialog will appear)
	// on iOS we can link directly to the App Store as our app switch will fire prior to the switch
	// If you have a mobile web app, your fallback could be that instead. 
    var fallbackLink = 'http://oursaviorgames.com';



    // Now we just wait for everything to execute, if the user is redirected to your custom app
    // the timeout below will never fire, if a custom app is not present (or the user is on the Desktop)
    // we will replace the current URL with the fallbackLink (store URL or desktop URL as appropriate)
    window.setTimeout(function (){ window.location.replace(fallbackLink); }, 120);


  })();</script>
</body>

</html>