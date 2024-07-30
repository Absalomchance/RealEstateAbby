package com.example.realestate.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.realestate.R;
import com.example.realestate.activities.LoginOptionsActivity;
import com.example.realestate.databinding.ActivityMainBinding;
import com.example.realestate.fragments.ChatListFragment;
import com.example.realestate.fragments.FavoriteistFragment;
import com.example.realestate.fragments.HomeFragment;
import com.example.realestate.fragments.ProfileFragment;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
private ActivityMainBinding binding;


private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null){
            startLoginOptionsActivity();
        }

        showHomeFragment();

        binding.bottomNavigationView.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {

                int itemId = item.getItemId();

                if (itemId == R.id.item_home){

                    showHomeFragment();
                } else if (itemId == R.id.item_chats){

                    showChatsListFragment();
                } else if (itemId == R.id.item_favorite) {

                    showFavoriteListFragment();
                } else if (itemId == R.id.item_profile) {

                    showProfileFragment();
            }
                //return true;

            }
        });
    }



    private void  showHomeFragment(){

        binding.toolbarTitleTv.setText("Home");


        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(),homeFragment);
        fragmentTransaction.commit();
    }

    private void showChatsListFragment(){

        binding.toolbarTitleTv.setText("Chats");

        ChatListFragment chatListFragment = new ChatListFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(),chatListFragment);
        fragmentTransaction.commit();
    }

    private void showFavoriteListFragment(){

        binding.toolbarTitleTv.setText("Favorites");

        FavoriteistFragment favoriteistFragment = new FavoriteistFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(),favoriteistFragment);
        fragmentTransaction.commit();
    }
    private void showProfileFragment(){

        binding.toolbarTitleTv.setText("Profile");

        ProfileFragment profileFragment = new ProfileFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(),profileFragment);
        fragmentTransaction.commit();
    }
    private void startLoginOptionsActivity() {
        startActivity(new Intent(this, LoginOptionsActivity.class));
    }
}