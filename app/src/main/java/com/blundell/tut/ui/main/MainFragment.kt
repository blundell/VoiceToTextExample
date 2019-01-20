package com.blundell.tut.ui.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blundell.tut.R
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.*
import com.google.protobuf.ByteString
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val speechClient: SpeechClient by lazy {
        // NOTE: TODO STOPSHIP The line below uses an embedded credential (res/raw/credential.json).
        //       You should not package a credential with real application.
        //       Instead, you should get a credential securely from a server.
        activity?.applicationContext?.resources?.openRawResource(R.raw.credential).use {
            SpeechClient.create(
                SpeechSettings.newBuilder()
                    .setCredentialsProvider { GoogleCredentials.fromStream(it) }
                    .build())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        GlobalScope.launch {
            Log.d("TUT", "I'm working in thread ${Thread.currentThread().name}")
            val filePath = activity!!.getExternalFilesDir("rec")?.toString() + "/demo_file.awb"
            analyze(ByteString.copyFrom(File(filePath).readBytes()))
        }
    }

    private fun analyze(fileByteString: ByteString) {
        val req = LongRunningRecognizeRequest.newBuilder()
            .setConfig(
                RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.AMR_WB)
                    .setLanguageCode("en-US")
                    .setSampleRateHertz(16000)
                    .build()
            )
            .setAudio(
                RecognitionAudio.newBuilder()
                    .setContent(fileByteString)
                    .build()
            )
            .build()
        Log.d("TUT", "Requesting")
        val responseFuture = speechClient.longRunningRecognizeAsync(req)
        val response: LongRunningRecognizeResponse = responseFuture.get()
        Log.d("TUT", "Response! Count ${response.resultsCount}")
        val results = response.resultsList
        for (result in results) {
            val alternative = result.getAlternativesList().get(0)
            val text = alternative.getTranscript()
            Log.d("TUT", "Transcription: $text")
        }
    }

}
