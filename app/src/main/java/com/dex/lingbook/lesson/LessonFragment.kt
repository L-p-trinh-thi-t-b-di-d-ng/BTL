package com.dex.lingbook.lesson

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dex.lingbook.R
import com.dex.lingbook.databinding.FragmentLessonBinding
import com.dex.lingbook.user.SocialActivity
import androidx.fragment.app.activityViewModels
import com.dex.lingbook.viewmodel.MainViewModel


class LessonFragment : Fragment() {
    private lateinit var binding: FragmentLessonBinding

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLessonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListener()
        setupSocialClick()
    }

    private fun setupClickListener() {
        binding.btnBeginnerStart.setOnClickListener {
            val bundle = bundleOf("levelName" to "Beginner")
            findNavController().navigate(R.id.action_to_lessonListFragment, bundle)
        }

        binding.btnIntermediateStart.setOnClickListener {
            val bundle = bundleOf("levelName" to "Intermediate")
            findNavController().navigate(R.id.action_to_lessonListFragment, bundle)
        }

        binding.btnAdvancedStart.setOnClickListener {
            val bundle = bundleOf("levelName" to "Advanced")
            findNavController().navigate(R.id.action_to_lessonListFragment, bundle)
        }
    }

    private fun setupSocialClick() {
        binding.ivFacebook.setOnClickListener {
            // 1. Lấy URL từ ViewModel (đã được load trong MainActivity)
            val avatarUrl = mainViewModel.user.value?.avatarUrl ?: ""

            // 2. Tạo Intent và truyền URL
            val intent = Intent(requireContext(), SocialActivity::class.java).apply {
                // Sử dụng hằng số đã định nghĩa trong SocialActivity
                putExtra(SocialActivity.EXTRA_AVATAR_URL, avatarUrl)
            }

            // 3. Khởi chạy Activity
            startActivity(intent)
        }
    }
}
