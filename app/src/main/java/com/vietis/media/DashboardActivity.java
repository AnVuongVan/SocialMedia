package com.vietis.media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.vietis.media.fragments.ChatListFragment;
import com.vietis.media.fragments.GroupChatsFragment;
import com.vietis.media.fragments.HomeFragment;
import com.vietis.media.fragments.NotificationsFragment;
import com.vietis.media.fragments.ProfileFragment;
import com.vietis.media.fragments.UsersFragment;
import com.vietis.media.notifications.Token;

public class DashboardActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private String mUID;
    private ActionBar actionBar;
    private BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        navigationView = findViewById(R.id.navigation_bottom_dash);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        actionBar.setTitle("Home");
        getSupportFragmentManager().beginTransaction().replace(R.id.content_dash, new HomeFragment()).commit();

        checkUserStatus();

    }

    public void updateToken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        if (mUID != null) {
            ref.child(mUID).setValue(mToken);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            actionBar.setTitle("Home");
                            getSupportFragmentManager().beginTransaction().replace(R.id.content_dash, new HomeFragment()).commit();
                            break;
                        case R.id.nav_profile:
                            actionBar.setTitle("Profile");
                            getSupportFragmentManager().beginTransaction().replace(R.id.content_dash, new ProfileFragment()).commit();
                            break;
                        case R.id.nav_users:
                            actionBar.setTitle("Users");
                            getSupportFragmentManager().beginTransaction().replace(R.id.content_dash, new UsersFragment()).commit();
                            break;
                        case R.id.nav_chat:
                            actionBar.setTitle("Chats");
                            getSupportFragmentManager().beginTransaction().replace(R.id.content_dash, new ChatListFragment()).commit();
                            break;
                        case R.id.nav_more:
                            showMoreOptions();
                            break;
                    }
                    return true;
                }
            };

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(this, navigationView, Gravity.END);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Notifications");
        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Group Chats");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == 0) {
                    actionBar.setTitle("Notifications");
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_dash, new NotificationsFragment()).commit();
                } else if (item.getItemId() == 1) {
                    actionBar.setTitle("Group Chats");
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_dash, new GroupChatsFragment()).commit();
                }
                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        } else {
            mUID = user.getUid();
            SharedPreferences preferences = getSharedPreferences("SP_USER", MODE_PRIVATE);
            @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = preferences.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();
            updateToken(FirebaseInstanceId.getInstance().getToken());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}