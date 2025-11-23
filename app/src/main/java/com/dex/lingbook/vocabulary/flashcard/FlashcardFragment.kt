package com.dex.lingbook.vocabulary.flashcard

import android.animation.Animator
import android.animation.AnimatorInflater
import android.annotation.SuppressLint
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.dex.lingbook.R
import com.dex.lingbook.databinding.FragmentFlashcardBinding
import com.dex.lingbook.model.Vocabulary
import com.dex.lingbook.model.VocabularyProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Locale

class FlashcardFragment : Fragment(), TextToSpeech.OnInitListener {

    private val TAG = "FlashcardFragment"
    private lateinit var binding: FragmentFlashcardBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var frontAnim: Animator
    private lateinit var backAnim: Animator
    private lateinit var tts: TextToSpeech

    // --- BIẾN TRẠNG THÁI ---
    private var vocabularyList: List<Vocabulary> = emptyList()
    private var currentCardIndex = 0
    private var isFrontVisible = true

    // --- BIẾN LOGIC VUỐT ---
    private var x1: Float = 0f
    private var x2: Float = 0f
    private val MIN_SWIPE_DISTANCE = 150 

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFlashcardBinding.inflate(inflater, container, false)
        tts = TextToSpeech(requireContext(), this)  
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFirebase()
        setupAnimations()
        fetchVocabulary()
        setupClickListeners()
    }

    private fun initFirebase() {
        db = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
    }

    // Thiết lập các animation cho thẻ
    private fun setupAnimations() {
        val scale = requireContext().resources.displayMetrics.density
        binding.cardFront.cameraDistance = 8000 * scale // KC vừa đủ
        binding.cardBack.cameraDistance = 8000 * scale
        frontAnim = AnimatorInflater.loadAnimator(requireContext(), R.animator.flip_out)
        backAnim = AnimatorInflater.loadAnimator(requireContext(), R.animator.flip_in)
    }

    // Implement hàm onInit của TTS
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "The specified language is not supported!")
                binding.btnSpeak.isEnabled = false
            }

            else {
                binding.btnSpeak.isEnabled = true
                Log.d(TAG, "TTS Initialized Successfully!")
                // Phát âm câu đầu tiên nếu có
                if (vocabularyList.isNotEmpty()) {
                    speakCurrentWord()
                }
            }
        } else {
            Log.e(TAG, "TTS Initialization Failed!")
            binding.btnSpeak.isEnabled = false
        }
    }

    //RA LỆNH CHO TTS ĐỌC TO TỪ VỰNG HIỆN TẠI
    private fun speakCurrentWord() {
        if (vocabularyList.isNotEmpty() && currentCardIndex in vocabularyList.indices) {
                                           //index >= 0 && index < list.size.
            val wordToSpeak = vocabularyList[currentCardIndex].word
            tts.speak(wordToSpeak, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    //ĐI TỪ ỨNG DỤNG LÊN FIREBASE ĐỂ LẤY TỪ VỰNG VỀ HIỂN THỊ
    // Sửa lại hàm này trong FlashcardFragment.kt
    private fun fetchVocabulary() {
        binding.progressBar.visibility = View.VISIBLE

        // 1. Nhận thêm targetWordId (ID của từ muốn nhảy đến)
        val topicName = arguments?.getString("topicName")
        val targetWordId = arguments?.getString("targetWordId")

        var query: Query = db.collection("vocabulary")

        if (!topicName.isNullOrEmpty()) {
            query = query.whereEqualTo("topic", topicName)
        }

        query.get().addOnSuccessListener { querySnapshot ->
            binding.progressBar.visibility = View.GONE
            if (!querySnapshot.isEmpty) {
                vocabularyList = querySnapshot.toObjects(Vocabulary::class.java)

                // --- ĐOẠN MỚI THÊM VÀO ---
                // Kiểm tra nếu có yêu cầu nhảy đến từ cụ thể
                if (!targetWordId.isNullOrEmpty()) {
                    // Tìm xem từ đó nằm ở vị trí số mấy trong danh sách
                    val targetIndex = vocabularyList.indexOfFirst { it.id == targetWordId }

                    // Nếu tìm thấy (index != -1) thì gán index hiện tại bằng nó
                    if (targetIndex != -1) {
                        currentCardIndex = targetIndex
                    }
                }
                // -------------------------

                displayCurrentCard()
            } else {
                Toast.makeText(requireContext(), "Chưa có từ vựng.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            binding.progressBar.visibility = View.GONE
            Log.e(TAG, "Lỗi tải từ vựng: $topicName", exception)
        }
    }

    //XÓA NỘI DUNG CŨ VÀ ĐƯA NỘI DUNG MỚI RA MÀN HÌNH
    private fun displayCurrentCard() {
        if (vocabularyList.isNotEmpty() && currentCardIndex in vocabularyList.indices) {
            val vocabulary = vocabularyList[currentCardIndex]
            //thanh ngang trên cùng màn hình để người dùng biết mình đã học được bao nhiêu phần trăm
            val progressPercentage = ((currentCardIndex + 1) * 100 / vocabularyList.size)
            binding.progressIndicator.progress = progressPercentage

            binding.tvWord.text = vocabulary.word           // Điền từ vựng (Hello)
            binding.tvPronunciation.text = vocabulary.pronunciation // Điền phát âm (/həˈlō/)
            binding.tvDefinition.text = vocabulary.definition // Điền nghĩa (Xin chào)
            binding.tvWordTypeBack.text = vocabulary.type     // Điền loại từ (Noun/Verb) ở mặt sau
            binding.tvExample.text = "Example: ${vocabulary.example}" // Điền câu ví dụ
            Glide.with(this).load(vocabulary.imageUrl).into(binding.ivWordImage)
            resetCard()
            speakCurrentWord()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    //LẬT THẺ
    private fun setupClickListeners() {
        val flipClickListener = View.OnClickListener { flipCard() }
        binding.cardFront.setOnClickListener(flipClickListener)
        binding.cardBack.setOnClickListener(flipClickListener)
        // Gán sự kiện lắng nghe thao tác vuốt cho khu vực chứa thẻ
        binding.cardContainer.setOnTouchListener { _, event ->
            handleSwipe(event)
            // Trả về false để các sự kiện click khác (như lật thẻ) vẫn hoạt động
            false
        }
        binding.btnIKnow.setOnClickListener { saveWordProgress(isLearned = true) }
        binding.btnDontKnow.setOnClickListener { saveWordProgress(isLearned = false) }
        binding.btnNext.setOnClickListener { handleNextWord() }
        binding.btnPrev.setOnClickListener { handlePrevWord() }
        binding.btnSpeak.setOnClickListener { speakCurrentWord() }
    }

    // --- HÀM XỬ LÝ VUỐT ---
    private fun handleSwipe(event: MotionEvent) {
        when (event.action) {
            // Khi người dùng bắt đầu chạm vào màn hình
            MotionEvent.ACTION_DOWN -> {
                x1 = event.x
            }
            // Khi người dùng nhấc ngón tay lên
            MotionEvent.ACTION_UP -> {
                x2 = event.x
                val deltaX = x2 - x1

                // Kiểm tra xem khoảng cách có đủ lớn để coi là một cú vuốt không
                if (deltaX < -MIN_SWIPE_DISTANCE) {
                    // Vuốt sang trái -> Qua từ tiếp theo
                    handleNextWord()
                } else if (deltaX > MIN_SWIPE_DISTANCE) {
                    // Vuốt sang phải -> Quay lại từ trước
                    handlePrevWord()
                }
            }
        }
    }

    private fun flipCard() {
        // Đang hiện mặt trước -> Muốn lật sang mặt sau
        if (isFrontVisible) {
            frontAnim.setTarget(binding.cardFront)
            backAnim.setTarget(binding.cardBack)
            frontAnim.start()
            backAnim.start()
            binding.cardBack.visibility = View.VISIBLE  // Bật mặt sau lên
            binding.cardFront.visibility = View.GONE   // Tắt mặt trước đi
        } else {
            frontAnim.setTarget(binding.cardBack)
            backAnim.setTarget(binding.cardFront)
            frontAnim.start()
            backAnim.start()
            binding.cardFront.visibility = View.VISIBLE
            binding.cardBack.visibility = View.GONE
        }
        isFrontVisible = !isFrontVisible
    }

    private fun handleNextWord() {
        if (currentCardIndex < vocabularyList.size - 1) {
            currentCardIndex++
            displayCurrentCard()
        } else {
            Toast.makeText(requireContext(), "Bạn đã xem hết từ vựng!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handlePrevWord() {
        if (currentCardIndex > 0) {
            currentCardIndex--
            displayCurrentCard()
        } else {
            Toast.makeText(requireContext(), "Đây là từ đầu tiên!", Toast.LENGTH_SHORT).show()
        }
    }

    // --- HÀM LƯU TIẾN ĐỘ TỪ VỰNG ---
    private fun saveWordProgress(isLearned: Boolean) {
        if (currentCardIndex !in vocabularyList.indices) return
        val uid = firebaseAuth.currentUser?.uid ?: return
        val wordId = vocabularyList[currentCardIndex].id

        setInteractionEnabled(false)

        // tối ưu hóa của Firestore, "Dot Notation" (Cập nhật lồng nhau)
        val progressData = VocabularyProgress(isLearned = isLearned)

        val fieldToUpdate = "vocabularyProgress.$wordId"

        db.collection("userProgress").document(uid)
            .update(fieldToUpdate, progressData)


            .addOnSuccessListener {
                Log.d(TAG, "Progress for word '$wordId' saved.")
                // Tự động chuyển sang từ tiếp theo để luồng học mượt mà
                handleNextWord()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error saving word progress", e)
                Toast.makeText(requireContext(), "Lỗi khi lưu tiến độ", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                setInteractionEnabled(true)
            }
    }

    // --- HÀM RESETCARD ---
    private fun resetCard() {
        // Hủy các animation đang chạy (nếu có) để tránh lỗi
        frontAnim.cancel()
        backAnim.cancel()

        isFrontVisible = true
        // Reset góc xoay về 0
        binding.cardFront.rotationY = 0f
        binding.cardBack.rotationY = 0f
        // Reset hiển thị về trạng thái ban đầu
        binding.cardFront.visibility = View.VISIBLE
        binding.cardBack.visibility = View.GONE
    }

    // NGĂN SPAM OR BẤM NHẦM
    private fun setInteractionEnabled(enabled: Boolean) {
        binding.btnIKnow.isEnabled = enabled
        binding.btnDontKnow.isEnabled = enabled
        binding.btnNext.isEnabled = enabled
        binding.btnPrev.isEnabled = enabled
    }

    override fun onDestroyView() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroyView()
    }
}
