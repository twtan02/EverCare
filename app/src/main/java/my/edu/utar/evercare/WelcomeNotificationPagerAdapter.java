package my.edu.utar.evercare;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class WelcomeNotificationPagerAdapter extends FragmentStateAdapter {

    public WelcomeNotificationPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return the WelcomeNotificationFragment with the page number
        return WelcomeNotificationFragment.newInstance(position + 1);
    }

    @Override
    public int getItemCount() {
        // Return the total number of pages
        return 2; // Adjust as needed
    }
}
