package com.example.testmaster

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.adapter.TestAppear_Adapter
import com.example.testmaster.model.AnswerKey
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var user : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var view: View
    lateinit var rv_testApear : RecyclerView
    lateinit var mark_piechart : PieChart
    lateinit var testappearAdapter : TestAppear_Adapter
    var recentExamApearList : MutableList<AnswerKey> = mutableListOf()
    var fullMarkList : MutableList<Float> = ArrayList()
    var attemptDateList : MutableList<String> = ArrayList()
    var scoredMarkList : MutableList<Float> = ArrayList()
    private lateinit var circularProgressIndicator: CircularProgressIndicator
    private lateinit var progressText: TextView
    private lateinit var view_all: TextView
    private lateinit var total_given_exam: TextView
    private lateinit var qualified_exam: TextView
    private lateinit var unqualified_exam: TextView
    private lateinit var ll_test_appear: LinearLayout
    var totalGivenExam = 0
    var qualifiedExam = 0
    var totalExamMark : Float = 0.0f
    var totalMarkScored : Float = 0.0f

    lateinit var barchart: BarChart
    private lateinit var barDataSet1: BarDataSet
    private lateinit var barDataSet2: BarDataSet


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_home, container, false)
        getHistoryList()
        circularProgressIndicator = view.findViewById(R.id.circularProgressIndicator)
        progressText = view.findViewById(R.id.progressText)
        rv_testApear  = view.findViewById(R.id.rv_testApear)
        barchart = view.findViewById(R.id.barchart)
        view_all = view.findViewById(R.id.view_all)
        total_given_exam = view.findViewById(R.id.total_given_exam)
        qualified_exam = view.findViewById(R.id.qualified_exam)
        unqualified_exam = view.findViewById(R.id.unqualified_exam)
        mark_piechart=view.findViewById(R.id.mark_piechart)
        ll_test_appear=view.findViewById(R.id.ll_test_appear)

        testappearAdapter = TestAppear_Adapter(view.context,recentExamApearList)
        rv_testApear.layoutManager = LinearLayoutManager(view.context,LinearLayoutManager.HORIZONTAL,false)
        rv_testApear.adapter=testappearAdapter
        if(recentExamApearList.isEmpty()){
            total_given_exam.text = "0"
            qualified_exam.text = "0"
            unqualified_exam.text = "0"
            val records = ArrayList<PieEntry>().apply {
                add(PieEntry(1f, "Score"))
                add(PieEntry(0f, "Lost"))
            }
            val dataSet = PieDataSet(records, "").apply {
                setColors(*ColorTemplate.COLORFUL_COLORS)
                valueTextColor = Color.BLACK
                valueTextSize = 15f
            }
            val pieData = PieData(dataSet)

            mark_piechart.apply {
                data = pieData
                legend.isEnabled = false
                setCenterTextSize(15f)
                setEntryLabelTextSize(12f)   // Adjust label text size
                description.isEnabled = false
                setCenterText("Total mark\n 1")
                animate()  // Start animation
                invalidate()  // Force re-draw to show data
            }
            barchart.invalidate()
            val percentage = (totalMarkScored / totalExamMark) * 100
            circularProgressIndicator.setProgress(100)
            circularProgressIndicator.setIndicatorColor(Color.DKGRAY)
            progressText.text = "100%"

            fullMarkList.add(0,50f)
            fullMarkList.add(1,50f)
            fullMarkList.add(2,50f)
            scoredMarkList.add(0,40f)
            scoredMarkList.add(1,30f)
            scoredMarkList.add(2,20f)
            setupBarChart()
            loadBarChartData(view)
        }
        return view
    }
    private fun setupBarChart() {
        with(barchart) {
            description.isEnabled = false
            setDragEnabled(true)
            setVisibleXRangeMaximum(3f)
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(attemptDateList)
                setCenterAxisLabels(true)
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                isGranularityEnabled = true
                axisMinimum = 0f
                axisMaximum = fullMarkList.size.toFloat()
            }
            zoom(1f, 1f, 0f, 0f)
            moveViewToX(fullMarkList.size.toFloat())
            animateY(1000)
        }
    }

    private fun loadBarChartData(view: View) {

        barDataSet1 = BarDataSet(getBarChartDataForSet1(), "Total Mark").apply {
            color = ContextCompat.getColor(view.context, R.color.blue)
        }
        barDataSet2 = BarDataSet(getBarChartDataForSet2(), "Mark Scored").apply {
            color = ContextCompat.getColor(view.context, R.color.blue_link)
        }

        val data = BarData(barDataSet1, barDataSet2).apply {
            barWidth = 0.15f
        }

        barchart.apply {
            this.data = data
            groupBars(0f, 0.5f, 0.1f)
            invalidate()
        }
    }

    private fun getBarChartDataForSet1(): ArrayList<BarEntry>{
        val FullMarkentries = ArrayList<BarEntry>()
        fullMarkList.forEachIndexed { index, mark ->
            FullMarkentries.add(BarEntry(index.toFloat(), mark))
        }
        return FullMarkentries
    }

    private fun getBarChartDataForSet2(): ArrayList<BarEntry> {
        val scoredMarkEntries = ArrayList<BarEntry>()
        scoredMarkList.forEachIndexed { index, mark ->
            scoredMarkEntries.add(BarEntry(index.toFloat(), mark))
        }
        return scoredMarkEntries
    }
    fun getHistoryList() {
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        user = firebaseAuth.currentUser?.uid.toString()
        db.collection("History").document(user).collection("HistoryDetails")
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    Log.w("Firestore", "Listen failed.", error)
                    return@addSnapshotListener
                }
                if (documents != null && !documents.isEmpty) {
                    fullMarkList.clear()
                    scoredMarkList.clear()
                    recentExamApearList.clear()
                    qualifiedExam = 0
                    totalMarkScored = 0f
                    totalExamMark = 0f
                    val tempList = mutableListOf<AnswerKey>()

                    for (document in documents) {
                        val answerKey = document.toObject(AnswerKey::class.java)
                        tempList.add(answerKey)
                    }

                    val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
                    tempList.sortWith { a, b ->
                        val dateA = a.attempt_date?.let { originalFormat.parse(it) } ?: Date(0)
                        val dateB = b.attempt_date?.let { originalFormat.parse(it) } ?: Date(0)
                        dateB.compareTo(dateA)  // Sort in descending order
                    }
                    for (answerKey in tempList) {
                        recentExamApearList.add(answerKey)
                        if (answerKey.pass_mark?.toDouble() ?: 0.0 <= answerKey.total_score?.toDouble() ?: 0.0) {
                            qualifiedExam += 1
                        }
                        totalMarkScored += answerKey.total_score?.toFloat() ?: 0f
                        totalExamMark += answerKey.pos_mark?.toFloat()?.times((answerKey.questionsWithAns?.size ?: 0)) ?: 0f
                        fullMarkList.add(answerKey.pos_mark?.toFloat()?.times((answerKey.questionsWithAns?.size ?: 0)) ?: 0f)
                        scoredMarkList.add(answerKey.total_score?.toFloat() ?: 0f)
                        val attempt_date = answerKey.attempt_date?.let { originalFormat.parse(it) }
                        val formated_date = attempt_date?.let { SimpleDateFormat("MM/YY", Locale.ENGLISH).format(it) }
                        formated_date?.let { attemptDateList.add(it) }
                    }

                    totalGivenExam = recentExamApearList.size
                    setData()
                    if (recentExamApearList.size > 5) {
                        recentExamApearList = recentExamApearList.takeLast(5).toMutableList()
                    }
                    testappearAdapter.notifyDataSetChanged()
                } else {
                    Log.d("Firestore", "No data found")
                }
            }
    }


    fun setData(){
        total_given_exam.text = totalGivenExam.toString()
        qualified_exam.text = qualifiedExam.toString()
        unqualified_exam.text = (totalGivenExam - qualifiedExam).toString()
        val records = ArrayList<PieEntry>().apply {
            add(PieEntry(totalMarkScored, "Score"))
            add(PieEntry(totalExamMark-totalMarkScored, "Lost"))
        }
        val dataSet = PieDataSet(records, "").apply {
            setColors(*ColorTemplate.COLORFUL_COLORS)
            valueTextColor = Color.BLACK
            valueTextSize = 15f
        }
        val pieData = PieData(dataSet)

        mark_piechart.apply {
            data = pieData
            legend.isEnabled = true
            setCenterTextSize(15f)
            setEntryLabelTextSize(12f)   // Adjust label text size
            description.isEnabled = false
            setCenterText("Total mark\n $totalExamMark")
            animate()  // Start animation
            invalidate()  // Force re-draw to show data
        }
        barchart.invalidate()
        val percentage = (totalMarkScored / totalExamMark) * 100
        circularProgressIndicator.setProgress(percentage.toInt())
        circularProgressIndicator.setIndicatorColor(Color.DKGRAY)
        progressText.text = "${percentage.toInt()}%"
        setupBarChart()
        loadBarChartData(view)
    }
    // Call this method when you want to change the ViewPager item
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}