package fr.harmoniamk.statsmk.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import fr.harmoniamk.statsmk.fragment.AddUserFragment
import fr.harmoniamk.statsmk.fragment.ConnectUserFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow

@FlowPreview
@ExperimentalCoroutinesApi
class WelcomePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    val onFinish = MutableSharedFlow<Unit>()
    val onNoCode = MutableSharedFlow<Unit>()

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> ConnectUserFragment(onFinish, onNoCode)
        else -> AddUserFragment(onFinish)
    }
}