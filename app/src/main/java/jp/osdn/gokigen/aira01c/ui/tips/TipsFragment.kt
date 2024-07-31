package jp.osdn.gokigen.aira01c.ui.tips

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import jp.osdn.gokigen.aira01c.R

class TipsFragment : Fragment()
{

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        val root: View = inflater.inflate(R.layout.fragment_tips, container, false)
        setupTipsSelection(root)

        return root
    }

    private fun setupTipsSelection(rootView: View)
    {
        try
        {
            val adapter = ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_spinner_item)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            adapter.add(getString(R.string.faq_title_00))
            adapter.add(getString(R.string.faq_title_01))
            adapter.add(getString(R.string.faq_title_02))
            adapter.add(getString(R.string.faq_title_03))
            adapter.add(getString(R.string.faq_title_04))
            adapter.add(getString(R.string.faq_title_05))
            adapter.add(getString(R.string.faq_title_06))
            adapter.add(getString(R.string.faq_title_07))
            adapter.add(getString(R.string.faq_title_08))

            val spinner: AppCompatSpinner = rootView.findViewById(R.id.tips_title_selection)
            spinner.adapter = adapter
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {
                    Log.v(TAG, "onItemSelected(parent: $parent, view: $view, pos: $pos, id: $id)")
                    if (view != null)
                    {
                        val question: AppCompatTextView? = rootView.findViewById(/* id = */ R.id.tips_question)
                        val answer : AppCompatTextView? = rootView.findViewById(/* id = */ R.id.tips_answer)
                        val image : AppCompatImageView? = rootView.findViewById(/* id = */ R.id.tips_image)
                        if ((question != null)&&(answer != null)&&(image != null)) {
                            when (pos) {
                                0 -> {
                                    question.setText(R.string.faq_question_00)
                                    answer.setText(R.string.faq_answer_00)
                                    image.setImageResource(R.drawable.air03)
                                }
                                1 -> {
                                    question.setText(R.string.faq_question_01)
                                    answer.setText(R.string.faq_answer_01)
                                    image.setImageResource(R.drawable.air09)
                                }
                                2 -> {
                                    question.setText(R.string.faq_question_02)
                                    answer.setText(R.string.faq_answer_02)
                                    image.setImageResource(R.drawable.air05)
                                }
                                3 -> {
                                    question.setText(R.string.faq_question_03)
                                    answer.setText(R.string.faq_answer_03)
                                    image.setImageResource(R.drawable.air06)
                                }
                                4 -> {
                                    question.setText(R.string.faq_question_04)
                                    answer.setText(R.string.faq_answer_04)
                                    image.setImageResource(R.drawable.air01)
                                }
                                5 -> {
                                    question.setText(R.string.faq_question_05)
                                    answer.setText(R.string.faq_answer_05)
                                    image.setImageResource(R.drawable.air08)
                                }
                                6 -> {
                                    question.setText(R.string.faq_question_06)
                                    answer.setText(R.string.faq_answer_06)
                                    image.setImageResource(R.drawable.air02)
                                }
                                7 -> {
                                    question.setText(R.string.faq_question_07)
                                    answer.setText(R.string.faq_answer_07)
                                    image.setImageResource(R.drawable.air04)
                                }
                                8 -> {
                                    question.setText(R.string.faq_question_08)
                                    answer.setText(R.string.faq_answer_08)
                                    image.setImageResource(R.drawable.air07)
                                }
                                else -> {
                                    question.setText(R.string.faq_question_00)
                                    answer.setText(R.string.faq_answer_00)
                                    image.setImageResource(R.drawable.air03)
                                }
                            }
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    Log.v(TAG, "onNothingSelected()")
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = TipsFragment::class.java.simpleName
    }
}
