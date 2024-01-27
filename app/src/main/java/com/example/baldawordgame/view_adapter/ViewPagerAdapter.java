package com.example.baldawordgame.view_adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.baldawordgame.fragment.GameCreationFragment;
import com.example.baldawordgame.fragment.GameListFragment;
import com.example.baldawordgame.fragment.ProfileFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ProfileFragment();
            case 1:
                return new GameListFragment();
            default:
                return new GameCreationFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }


}
