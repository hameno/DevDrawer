package de.psdev.devdrawer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import de.psdev.devdrawer.analytics.TrackingService
import javax.inject.Inject

abstract class BaseFragment<T : ViewBinding> : Fragment() {

    @Inject
    lateinit var trackingService: TrackingService

    private var _binding: T? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    protected val binding get() = _binding!!

    protected var toolbarTitle: CharSequence
        get() = requireActivity().title
        set(value) {
            requireActivity().title = value
        }

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = createViewBinding(inflater, container, savedInstanceState).also { viewBinding ->
        _binding = viewBinding
    }.root

    protected abstract fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): T

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected fun updateToolbarTitle(@StringRes resId: Int) {
        requireActivity().setTitle(resId)
        trackingService.trackScreen(this::class.java, getString(resId))
    }

    val Fragment.viewLifecycleScope: LifecycleCoroutineScope
        get() = viewLifecycleOwner.lifecycleScope

}
