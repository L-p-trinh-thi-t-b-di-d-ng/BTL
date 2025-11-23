package com.dex.lingbook.progress

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController // Import cái này
import androidx.recyclerview.widget.LinearLayoutManager
import com.dex.lingbook.R
import com.dex.lingbook.progress.adapter.LearnedWordAdapter
import com.dex.lingbook.databinding.FragmentLearnedWordsBinding
import com.dex.lingbook.model.UserProgress
import com.dex.lingbook.model.Vocabulary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class LearnedWordsFragment : Fragment() {
    private val TAG = "LearnedWordsFragment"
    private lateinit var binding: FragmentLearnedWordsBinding
    private lateinit var learnedWordAdapter: LearnedWordAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLearnedWordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        fetchLearnedWords()
    }

    private fun setupRecyclerView() {
        // Khởi tạo Adapter với hàm callback xử lý click
        learnedWordAdapter = LearnedWordAdapter(emptyList()) { clickedWord ->
            // Chuẩn bị dữ liệu để gửi sang Flashcard
            val bundle = Bundle()
            bundle.putString("topicName", clickedWord.topic)
            bundle.putString("targetWordId", clickedWord.id)

            // Chuyển trang
            findNavController().navigate(R.id.flashcardFragment, bundle)
        }

        binding.rvLearnedWords.apply {
            adapter = learnedWordAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun fetchLearnedWords() {
        binding.progressBar.visibility = View.VISIBLE
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("userProgress").document(uid).get()
            .addOnSuccessListener { document ->
                val userProgress = document.toObject(UserProgress::class.java)
                val learnedWordsMap = userProgress?.vocabularyProgress?.filter { it.value.isLearned }

                if (!learnedWordsMap.isNullOrEmpty()) {
                    fetchWordDetails(learnedWordsMap.keys.toList())
                } else {
                    binding.progressBar.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Log.e(TAG, "Error fetching user progress for words", it)
            }
    }

    private fun fetchWordDetails(wordIds: List<String>) {
        val chunks = wordIds.chunked(10)
        val allLearnedWords = mutableListOf<Vocabulary>()
        var tasksCompleted = 0

        chunks.forEach { chunk ->
            db.collection("vocabulary")
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .addOnSuccessListener { wordSnapshots ->
                    val words = wordSnapshots.toObjects(Vocabulary::class.java)
                    allLearnedWords.addAll(words)
                    tasksCompleted++

                    if (tasksCompleted == chunks.size) {
                        learnedWordAdapter.updateData(allLearnedWords)
                        binding.progressBar.visibility = View.GONE
                    }
                }
                .addOnFailureListener {
                    tasksCompleted++
                    if (tasksCompleted == chunks.size) binding.progressBar.visibility = View.GONE
                }
        }
    }
}
