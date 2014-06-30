package com.example.SnapJoinAndroid;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.nibletsAndroid.R;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    Button postbutton;
    Button getButton;
    Button loginButton;
    TextView jsonview;

    EditText emailText;
    EditText passwordText;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //initial EditText
        emailText = (EditText)this.findViewById(R.id.emailText);emailText.setText("ki@gmail.com");
        passwordText = (EditText)this.findViewById(R.id.passwdText);passwordText.setText("mypassword");


        jsonview = (TextView) this.findViewById(R.id.jsonview);
        postbutton = (Button) this.findViewById(R.id.button);
        postbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jsonview.setText("");
                AsyncTask<String, Void, String> result = new CreateUser().execute(getString(R.string.all_user_url));

            }
        });

        getButton = (Button) this.findViewById(R.id.GetButton);
        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jsonview.setText("");
                AsyncTask<String, Void, String> result = new GetAllUsers().execute(getString(R.string.all_user_url)+"/all");
            }
        });

        loginButton = (Button)this.findViewById(R.id.loginbutton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jsonview.setText("");
                String inputEmail = emailText.getText().toString();
                String inputPassword = passwordText.getText().toString();
              if(inputEmail.equals("")){
                 Toast.makeText(getApplicationContext(),"Please type your email",Toast.LENGTH_SHORT).show();
              }else if(inputPassword.equals("")){
                  Toast.makeText(getApplicationContext(),"Please type your password",Toast.LENGTH_SHORT).show();
              }
                String url = getString(R.string.all_user_url)+"/"+inputEmail +"/"+inputPassword+"/"+getString(R.string.user_by_email_password);

                AsyncTask<String,Void,String> ressult = new FindUserByEmailandPassword().execute(url);
            }
        });

    }

    /**
     * HTTP POST: create new User
     */
    class CreateUser extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            JSONObject jsonObject = new JSONObject();
            try {
                /**
                 * TODO: will change this later by dynamic from Android UI
                 */
                jsonObject.accumulate("username", "user.test.findbyemailandpassword");
                jsonObject.accumulate("email","ki@gmail.com");
                jsonObject.accumulate("password", "mypassword");
                jsonObject.accumulate("authToken","");
                jsonObject.accumulate("photoUrl", "www.imurg.com/myphotourl");
                jsonObject.accumulate("thumbnailUrl", "www.imurg.com/myphotourlthrumbbail");
            } catch (JSONException e) {
                e.printStackTrace();
            } catch(Exception ex){
                ex.printStackTrace();
            }

            return POST(url[0], jsonObject);
        }


        /**
         * Return the New User ID as Json
         *
         * @param result
         */
        protected void onPostExecute(String result) {
            //Parse Json object
            try {
                JSONObject jsonObject = new JSONObject(result);
                String userId = jsonObject.getString("userId");
                if (userId != null) {

                    jsonview.setText("User with user ID " + userId + " added successfully! ");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * HTTP GET: find user by email and password, will use for log in
     */
    class FindUserByEmailandPassword extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... urls) {
            return GET(urls[0]);
        }

        protected void onPostExecute(String result){
            //Parse Json object

            JSONObject jsonObject = null;
            try {

              if(result ==null || result.contains("ERROR")){
                  Toast.makeText(getApplicationContext(),"Oops...Email and Password do not match",Toast.LENGTH_SHORT).show();
                  return ;
              }
               jsonObject = new JSONObject(result);

                StringBuilder sb = new StringBuilder();

                    String id = jsonObject.getString("id");
                    String username = jsonObject.getString("username");
                    String password = jsonObject.getString("password");
                    String createdTime = jsonObject.getString("createdTime");
                    String photoId = jsonObject.getString("photoId");
                    String pollIds = jsonObject.getString("pollIds");
                    sb.append(id).append(" " + username + " " + password + " " + createdTime + " " + photoId + " " + pollIds).append("\n");


                if (jsonObject != null) {

                    jsonview.setText("find user by email and password \n" + sb.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



    /**
     * HTTP GET: get all users
     */
    class GetAllUsers extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            return GET(urls[0]);
        }

        protected void onPostExecute(String result) {
            //Parse Json object

            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(result);

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < jsonArray.length(); i++) {
                    String id = jsonArray.getJSONObject(i).getString("id");
                    String username = jsonArray.getJSONObject(i).getString("username");
                    String password = jsonArray.getJSONObject(i).getString("password");
                    String createdTime = jsonArray.getJSONObject(i).getString("createdTime");
                    String photoId = jsonArray.getJSONObject(i).getString("photoId");
                    String pollIds = jsonArray.getJSONObject(i).getString("pollIds");
                    sb.append(id).append(" " + username + " " + password + " " + createdTime + " " + photoId + " " + pollIds).append("\n");
                }

                if (jsonArray != null) {

                    jsonview.setText("Here are all users: \n" + sb.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    private String GET(String url) {
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make GET request to the given URL
            HttpGet httpGet = new HttpGet(url);

            // 7. Set some headers to inform server about the type of the content
            // httpPost.setHeader("Accept", "application/json");
            //` httpGet.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpGet);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    /**
     * Sending the HTTP POST requestion to REST web service
     *
     * @param url
     * @param jsonObject
     * @return
     */
    private String POST(String url, JSONObject jsonObject) {
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            // 3. build jsonObject


            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            // httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }


}
