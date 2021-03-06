package com.example.tickit.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.tickit.R;
import com.example.tickit.accounts.LoginActivity;
import com.example.tickit.databinding.ActivityMainBinding;
import com.example.tickit.tripmanager.CreateFragment;
import com.example.tickit.tripmanager.FeaturedTripsFragment;
import com.example.tickit.tripmanager.TripsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mBinding;
    MenuItem mMiActionProgressItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = mBinding.getRoot();
        setContentView(view);

        mBinding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.action_explore:
                        fragment = new ExploreTripsFragment();
                        Toast.makeText(MainActivity.this, R.string.exploreTrip, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_create:
                        fragment = new CreateFragment();
                        ((CreateFragment) fragment).setPostTransitionCallback(new CreateFragment.PostTransitionCallback() {
                            @Override
                            public void goTripsFragment() {
                                getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new TripsFragment()).commit();
                                mBinding.bottomNavigation.setSelectedItemId(R.id.action_trips);
                            }
                        });
                        Toast.makeText(MainActivity.this, R.string.createTrip, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_trips:
                    default:
                        fragment = new ProfileFragment();
                        Toast.makeText(MainActivity.this, R.string.myTrips, Toast.LENGTH_SHORT).show();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        mBinding.bottomNavigation.setSelectedItemId(R.id.action_explore);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mMiActionProgressItem = menu.findItem(R.id.miActionProgress);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.logout) {
            onLogoutClicked();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean onLogoutClicked() {
        ParseUser.logOut();
        Toast.makeText(this, R.string.logout, Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
        return true;
    }
}