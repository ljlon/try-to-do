var gh = (function() {
  'use strict';

  var signin_button;
  var revoke_button;
  var user_name_elem;
  var user_info_elem;

  var clientId = '100516718';
  var redirectUri =  'https://' + chrome.runtime.id + '.chromiumapp.org/provider_cb';
  
  var tokenFetcher = (function() {
	
    var redirectRe = new RegExp(redirectUri + '[#\?](.*)');

    var access_token = null;

    return {
      getToken: function(interactive, callback) {
        // In case we already have an access_token cached, simply return it.
        if (access_token) {
          callback(null, access_token);
          return;
        }

        var options = {
          'interactive': interactive,
          url:'https://graph.qq.com/oauth2.0/authorize?client_id=' + clientId +
              '&response_type=token' +
              '&redirect_uri=' + encodeURIComponent(redirectUri)
        }
        chrome.identity.launchWebAuthFlow(options, function(redirectUri) {
          console.log('launchWebAuthFlow completed', chrome.runtime.lastError,
              redirectUri);

          if (chrome.runtime.lastError) {
            callback(new Error(chrome.runtime.lastError));
            return;
          }
		  
          var matches = redirectUri.match(redirectRe);
          if (matches && matches.length > 1)
            handleProviderResponse(parseRedirectFragment(matches[1]));
          else
            callback(new Error('Invalid redirect URI'));
        });

        function parseRedirectFragment(fragment) {
          var pairs = fragment.split(/&/);
          var values = {};

          pairs.forEach(function(pair) {
            var nameval = pair.split(/=/);
            values[nameval[0]] = nameval[1];
          });

          return values;
        }

        function handleProviderResponse(values) {
          console.log('providerResponse', values);
          if (values.hasOwnProperty('#access_token'))
            setAccessToken(values['#access_token']);
          else 
            callback(new Error('Neither access_token nor code avialable.'));
        }

        function setAccessToken(token) {
          access_token = token; 
          console.log('Setting access_token: ', access_token);
          callback(null, access_token);
        }
      },

      removeCachedToken: function() {
          access_token = null;
      }
    }
  })();

  var openidFetcher = (function() {
    // Replace clientId and clientSecret with values obtained by you for your
	
    var redirectRe = new RegExp(redirectUri + '[#\?](.*)');
	var open_id = null;

    return {
      getOpenid: function(access_token, interactive, callback) {
        // In case we already have an open_id cached, simply return it.
        if (open_id) {
          callback(null, open_id);
          return;
        }
         
		var url = 'https://graph.z.qq.com/moc2/me?access_token=' + access_token;

		var xhr = new XMLHttpRequest();
		xhr.open('GET', url);
		xhr.onload = reqOpenIDComplete;
		xhr.send();

		function reqOpenIDComplete() {
		  console.log('reqOpenIDComplete', this.status, this.response);
		  if ( ( this.status < 200 || this.status >=300 ) && retry) {
			
		  } else {
			handleProviderResponse(parseRedirectFragment(this.response));
		  }
		}

        function parseRedirectFragment(fragment) {
          var pairs = fragment.split(/&/);
          var values = {};

          pairs.forEach(function(pair) {
            var nameval = pair.split(/=/);
            values[nameval[0]] = nameval[1];
          });

          return values;
        }

        function handleProviderResponse(values) {
          console.log('providerResponse', values);
          if (values.hasOwnProperty('openid'))
            setOpenID(values['openid']);
          else 
            callback(new Error('Neither open_id nor code avialable.'));
        }

        function setOpenID(openID) {
          open_id = openID; 
          console.log('Setting openid: ', open_id);
          callback(null, open_id);
        }
      },

      removeCachedOpenID: function() {
          open_id = null;
      }
    }
  })();
  
  function xhrWithAuth(method, url, interactive, callback) {
    var retry = true;
    var access_token;
	var openid;
	var wholeUrl;

    console.log('xhrWithAuth', method, url, interactive);
    getTokenAndOpenid();
	
    function getTokenAndOpenid() {
      tokenFetcher.getToken(interactive, function(error, token) {
        console.log('token fetch', error, token);
        if (error) {
          callback(error);
          return;
        }
        access_token = token;
		openidFetcher.getOpenid(access_token, interactive, function(error, token) {
			console.log('token fetch', error, token);
			if (error) {
			  callback(error);
			  return;
			}
			openid = token;
			wholeUrl = url + '?access_token=' + access_token +
				'&oauth_consumer_key=' + clientId +
				'&openid=' +  openid +
				'&format=json';
			requestStart();
		  });
      });
    }

    function requestStart() {
      var xhr = new XMLHttpRequest();
      xhr.open(method, wholeUrl);
      //xhr.setRequestHeader('Authorization', 'Bearer ' + access_token);
      xhr.onload = requestComplete;
      xhr.send();
    }

    function requestComplete() {
      console.log('requestComplete', this.status, this.response);
      if ( ( this.status < 200 || this.status >=300 ) && retry) {
        retry = false;
        tokenFetcher.removeCachedToken();
        access_token = null;
        getToken();
      } else {
        callback(null, this.status, this.response);
      }
    }
  }

  function getUserInfo(interactive) {
    xhrWithAuth('GET',
                'https://graph.qq.com/user/get_user_info',
                interactive,
                onUserInfoFetched);
  }

  // Functions updating the User Interface:

  function showButton(button) {
    button.style.display = 'inline';
    button.disabled = false;
  }

  function hideButton(button) {
    button.style.display = 'none';
  }

  function disableButton(button) {
    button.disabled = true;
  }

  function onUserInfoFetched(error, status, response) {
    if (!error && status == 200) {
      console.log("Got the following user info: " + response);
      var user_info = JSON.parse(response);
      populateUserInfo(user_info);
      hideButton(signin_button);
      showButton(revoke_button);
    } else {
      console.log('infoFetch failed', error, status);
      showButton(signin_button);
    }
  }

  function populateUserInfo(user_info) {
    var elem = user_name_elem;
    var nameElem = document.createElement('div');
    nameElem.innerHTML = "<b>Hello " + user_info.nickname + "</b><br>";
    elem.appendChild(nameElem);
	
    user_info_elem.value='';
	for (var i in user_info){
		user_info_elem.value += i + ":" + user_info[i] + "\n";
	}
  }

  // Handlers for the buttons's onclick events.

  function interactiveSignIn() {
    disableButton(signin_button);
    tokenFetcher.getToken(true, function(error, access_token) {
      if (error) {
        showButton(signin_button);
      } else {
        getUserInfo(true);
      }
    });
  }

  function revokeToken() {
    // We are opening the web page that allows user to revoke their token.
    window.open('http://connect.qq.com/toc/auth_manager');
    // And then clear the user interface, showing the Sign in button only.
    // If the user revokes the app authorization, they will be prompted to log
    // in again. If the user dismissed the page they were presented with,
    // Sign in button will simply sign them in.
    user_name_elem.textContent = '';
	user_info_elem.value = '';
    hideButton(revoke_button);
    showButton(signin_button);
	tokenFetcher.removeCachedToken();
	openidFetcher.removeCachedOpenID();
  }

  return {
    onload: function () {
      signin_button = document.querySelector('#signin');
      signin_button.onclick = interactiveSignIn;

      revoke_button = document.querySelector('#revoke');
      revoke_button.onclick = revokeToken;

      user_name_elem = document.querySelector('#user_name');
	  user_info_elem = document.querySelector('#user_info');
	  
      console.log(signin_button, revoke_button, user_name);

      showButton(signin_button);
      getUserInfo(false);
    }
  };
})();


window.onload = gh.onload;
