package fr.kevincaron.pierrelouisCapellier;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class MainActivity extends AppCompatActivity {

    private static final String IMGUR_LOGIN_URL = "https://api.imgur.com/oauth2/authorize?client_id=b93b06de61a4531&response_type=token&state=APPLICATION_STATE";
    private OkHttpClient httpClient;
    private static final String TAG = "MyActivity";
    private static class Photo {
        String id;
        String title;
    }
    ImageView profilePicture;
    Button acc;
    Button fav;
    Button actu;
    Button userImg;
    String accessToken;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        final WebView imgurWebView = (WebView) findViewById(R.id.LoginWebView);
        imgurWebView.setBackgroundColor(Color.TRANSPARENT);
        imgurWebView.loadUrl(IMGUR_LOGIN_URL);
        imgurWebView.getSettings().setJavaScriptEnabled(true);

        imgurWebView.setWebViewClient(new WebViewClient() {
              private static final String REDIRECT_URL = "https://www.getpostman.com/oauth2/callback";

              @Override
              public boolean shouldOverrideUrlLoading(WebView view, String url) {

                  if (url.contains(REDIRECT_URL)) {
                      setContentView(R.layout.content);
                      splitUrl(url, view);
                  } else {
                      view.loadUrl(url);
                  }

                  return true;
              }
          });
    }

    private void splitUrl(String url, WebView view) {
        fav = findViewById(R.id.button_fav);
        acc = findViewById(R.id.button_compte);
        actu = findViewById(R.id.button_actu);
        userImg = findViewById(R.id.button_userimage);

        String[] outerSplit = url.split("#")[1].split("&");
        String username = null;
        accessToken = null;
        String refreshToken = null;

        int index = 0;

        for (String s : outerSplit) {
            String[] innerSplit = s.split("=");

            switch (index) {
                // Access Token
                case 0:
                    accessToken = innerSplit[1];
                    break;

                // Refresh Token
                case 3:
                    refreshToken = innerSplit[1];
                    break;

                // Username
                case 4:
                    username = innerSplit[1];
                    break;
                default:

            }
            index++;
        }

        fetchData(username, accessToken);
        final String finalUsername = username;
        final String finalAccessToken = accessToken;
        fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favory(finalUsername, finalAccessToken);
            }
        });

        acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                account(finalUsername, finalAccessToken);
            }
        });

        userImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userImages(finalUsername, finalAccessToken);
            }
        });
    }

    private void fetchData(final String username, final String accessToken) {
        //https://api.imgur.com/3/account/{{username}}/images/{{page}}
        fav = findViewById(R.id.button_fav);
        acc = findViewById(R.id.button_compte);
        userImg = findViewById(R.id.button_userimage);

        fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favory(username, accessToken);
            }
        });

        acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                account(username, accessToken);
            }
        });

        userImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userImages(username, accessToken);
            }
        });

        httpClient = new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/gallery/user/rising/0.json")
                .header("Authorization","Client-ID b93b06de61a4531")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "*****************************************     An error has occurred " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, "*****************************************     IS GOOD ");
                JSONObject data;
                try {
                    data = new JSONObject(response.body().string());
                    JSONArray items = null;
                    items = data.getJSONArray("data");
                    final List<Photo> photos = new ArrayList<Photo>();

                    for(int i=0; i<items.length();i++) {
                        JSONObject item = null;
                        try {
                            item = items.getJSONObject(i);

                            Photo photo = new Photo();
                            if (item.getBoolean("is_album")) {
                                photo.id = item.getString("cover");
                            } else {
                                photo.id = item.getString("id");
                            }
                            photo.title = item.getString("title");
                            photos.add(photo); // Add photo to list
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    render(photos);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void account(final String username, final String accessToken) {
        setContentView(R.layout.account);
        profilePicture = findViewById(R.id.pp);
        fav = findViewById(R.id.button_fav);
        actu = findViewById(R.id.button_actu);
        userImg = findViewById(R.id.button_userimage);

        fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favory(username, accessToken);
            }
        });
        actu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.content);
                fetchData(username, accessToken);
            }
        });
        userImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userImages(username, accessToken);
            }
        });

        final TextView usr = findViewById(R.id.usr);
        final TextView bio = findViewById(R.id.bio);
        final TextView rep = findViewById(R.id.reputation);

        httpClient = new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/account/" + username)
                .header("Authorization","Client-ID b93b06de61a4531")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "*****************************************     An error has occurred " + e);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, "*****************************************     IS GOOD 3");

                JSONObject obj = null;
                final String data;

                try {
                    obj = new JSONObject(response.body().string());
                    data = obj.getString("data");
                    final JSONObject test = new JSONObject(data);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                usr.setText(username);
                                bio.setText(test.getString("bio"));
                                rep.setText(test.getString("reputation_name"));
                                Picasso.with(MainActivity.this).load(test.getString("avatar")).into(profilePicture);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void favory(final String username, final String accessToken) {
        //https://api.imgur.com/3/account/{{username}}/gallery_favorites/{{page}}/{{favoritesSort}}

        setContentView(R.layout.favory);
        actu = findViewById(R.id.button_actu);
        acc = findViewById(R.id.button_compte);
        userImg = findViewById(R.id.button_userimage);

        actu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.content);
                fetchData(username, accessToken);
            }
        });
        acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                account(username, accessToken);
            }
        });
        userImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userImages(username, accessToken);
            }
        });

        httpClient = new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/account/" + username + "/favorites")
                .header("Authorization","Bearer " + accessToken)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "*****************************************     An error has occurred " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, "*****************************************     IS GOOD 4");
                JSONObject data;
                try {
                    data = new JSONObject(response.body().string());
                    JSONArray items = null;
                    items = data.getJSONArray("data");
                    final List<Photo> photos = new ArrayList<Photo>();

                    for(int i=0; i<items.length();i++) {
                        JSONObject item = null;
                        try {
                            item = items.getJSONObject(i);

                            Photo photo = new Photo();
                            if (item.getBoolean("is_album")) {
                                photo.id = item.getString("cover");
                            } else {
                                photo.id = item.getString("id");
                            }
                            photo.title = item.getString("title");
                            photos.add(photo); // Add photo to list
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    render(photos);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void userImages(final String username, final String accessToken) {
        //https://api.imgur.com/3/account/{{username}}/images/{{page}}

        setContentView(R.layout.user_images);
        actu = findViewById(R.id.button_actu);
        fav = findViewById(R.id.button_fav);
        acc = findViewById(R.id.button_compte);

        fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favory(username, accessToken);
            }
        });
        actu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.content);
                fetchData(username, accessToken);
            }
        });
        acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                account(username, accessToken);
            }
        });

        httpClient = new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/account/" + username + "/images")
                .header("Authorization","Bearer " + accessToken)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "*****************************************     An error has occurred " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, "*****************************************     IS GOOD 4");
                JSONObject data;
                try {
                    data = new JSONObject(response.body().string());
                    JSONArray items = null;
                    items = data.getJSONArray("data");
                    final List<Photo> photos = new ArrayList<Photo>();

                    for(int i=0; i<items.length();i++) {
                        JSONObject item = null;
                        try {
                            item = items.getJSONObject(i);

                            Photo photo = new Photo();
                            photo.id = item.getString("id");
                            photo.title = item.getString("description");
                            photos.add(photo); // Add photo to list
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    render(photos);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class PhotoVH extends RecyclerView.ViewHolder {
        ImageView photo;
        TextView title;
        Button fav;
        String id;

        public PhotoVH(View itemView) {
            super(itemView);
            fav = itemView.findViewById(R.id.fav);
            fav.setOnClickListener(v -> {
                httpClient = new OkHttpClient.Builder().build();
                RequestBody bdy = new RequestBody() {
                    @Nullable
                    @Override
                    public MediaType contentType() {
                        return null;
                    }


                    @Override
                    public void writeTo(BufferedSink sink) throws IOException {

                    }
                };
                Request request = new Request.Builder()
                        .url("https://api.imgur.com/3/image/"+ id +"/favorite")
                        .header("Authorization", "Bearer "+ accessToken)
                        .post(bdy)
                        .build();
                httpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("Error", "Fail Request");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.e("Data", response.body().string());
                    }
                });
            });

        }
    }

    private void render(final List<Photo> photos) {
        RecyclerView rv = (RecyclerView)findViewById(R.id.rv_of_photos);
        rv.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView.Adapter<PhotoVH> adapter = new RecyclerView.Adapter<PhotoVH>() {
            @Override
            public PhotoVH onCreateViewHolder(ViewGroup parent, int viewType) {
                PhotoVH vh = new PhotoVH(getLayoutInflater().inflate(R.layout.test, null));
                vh.photo = (ImageView) vh.itemView.findViewById(R.id.photo);
                vh.title = (TextView) vh.itemView.findViewById(R.id.title);
                return vh;
            }

            @Override
            public void onBindViewHolder(PhotoVH holder, int position) {
                Picasso.with(MainActivity.this).load("https://imgur.com/" +
                        photos.get(position).id + ".jpg").into(holder.photo);
                holder.title.setText(photos.get(position).title);
                holder.id = photos.get(position).id;
            }

            @Override
            public int getItemCount() {
                return photos.size();
            }
        };

        rv.setAdapter(adapter);

        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = 16; // Gap of 16px
            }
        });
    }
}