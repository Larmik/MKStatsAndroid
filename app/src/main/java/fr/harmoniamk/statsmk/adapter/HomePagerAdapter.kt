package fr.harmoniamk.statsmk.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import fr.harmoniamk.statsmk.enums.MainSections

class HomePagerAdapter(val fa: FragmentActivity) : FragmentStateAdapter(fa) {

    fun getTabTitle(position: Int): String {
        return fa.getString(MainSections.values()[position].label)
    }

    override fun getItemCount(): Int {
        return MainSections.values().size
    }

    override fun createFragment(position: Int): Fragment {
        return MainSections.values()[position].fragment
    }
}