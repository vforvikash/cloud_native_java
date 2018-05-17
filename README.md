# cloud_native_java

Calling Auth service:
POST x-www-form-urlencoded data to http://localhost:9191/uaa/oauth/token. 
Request data:
password:vikash
username:hetal
grant_type:password
scope:openid
client_id:html5
client_secret:secret

Add authorization for auth-server (username: html5, password: secret).
Headers: Content: x-www-form-urlencoded, Accept: application/json

Use curl to call reservation service:
 curl -d'{ "reservationName": "Vikash" }' -H"content-type: application/json" http://localhost:9999/reservations
