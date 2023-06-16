package com.example.projectcapstone.ui.tryon

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.projectcapstone.ui.CameraActivity
import com.example.projectcapstone.R
import com.example.projectcapstone.byteArrayToFile
import com.example.projectcapstone.databinding.FragmentTryonBinding
import com.example.projectcapstone.fileToByteArray
import com.example.projectcapstone.networking.ApiConfig
import com.example.projectcapstone.reduceFileImage
import com.example.projectcapstone.reduceFileImageFast
import com.example.projectcapstone.response.TryonResponse
import com.example.projectcapstone.rotateFileForCamera
import com.example.projectcapstone.rotateFileForGallery
import com.example.projectcapstone.ui.ResultFragment
import com.example.projectcapstone.uriToFile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class TryonFragment : Fragment() {
    companion object{
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSION = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
        const val EXTRA_PHOTO = "extra_photo"
    }
    private var _binding: FragmentTryonBinding? = null
    private val binding get() = _binding
    private var file1 : File? = null
    private var file2 : File? = null
    private var _viewModel: TryonViewModel? = null
    private val viewModel get() = _viewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(
                requireActivity(),
                resources.getString(R.string.permission_not_granted),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if(it.resultCode == CAMERA_X_RESULT){
            val myFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.data?.getSerializableExtra("picture", File::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.data?.getSerializableExtra("picture")
            } as File
            file1 = myFile
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean
            val rotation = it.data?.getIntExtra("rotation", 0) as Int

            rotateFileForCamera(
                myFile,
                isBackCamera,
                rotation
            )
            val compressedFile = reduceFileImageFast(myFile)
            binding?.imgPreviewClothes?.setImageBitmap(BitmapFactory.decodeFile(compressedFile.path))
        }
    }

    private val launcherIntentCameraX2 = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if(it.resultCode == CAMERA_X_RESULT){
            val myFile2 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.data?.getSerializableExtra("picture", File::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.data?.getSerializableExtra("picture")
            } as File
            file2 = myFile2
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean
            val rotation = it.data?.getIntExtra("rotation", 0) as Int
            rotateFileForCamera(
                myFile2,
                isBackCamera,
                rotation
            )
            val compressedFile = reduceFileImageFast(myFile2)
            binding?.imgPreview?.setImageBitmap(BitmapFactory.decodeFile(compressedFile.path))


        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if (it.resultCode == AppCompatActivity.RESULT_OK){
            val selectedImg : Uri = it.data?.data as Uri
            val myFile = uriToFile(selectedImg, requireActivity())
            rotateFileForGallery(myFile)
            val compressed = reduceFileImageFast(myFile)
            file1 = compressed
            binding?.imgPreviewClothes?.setImageURI(selectedImg)
        }
    }

    private val launcherIntentGallery2 = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if (it.resultCode == AppCompatActivity.RESULT_OK){
            val selectedImg2 : Uri = it.data?.data as Uri

            val myFile2 = uriToFile(selectedImg2, requireActivity())
            rotateFileForGallery(myFile2)
            val compressed = reduceFileImageFast(myFile2)
            file2 = compressed
            binding?.imgPreview?.setImageURI(selectedImg2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val tryonViewModel =
            ViewModelProvider(this).get(TryonViewModel::class.java)

        _binding = FragmentTryonBinding.inflate(inflater, container, false)
        return binding?.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            val byteArray = savedInstanceState.getByteArray("myFile")
            if (byteArray != null) {
                file1 = byteArrayToFile(requireActivity(), byteArray)
                val myFile1 = file1 as File
                file2 = byteArrayToFile(requireActivity(), byteArray)
                val myFile2 = file2 as File
                binding?.imgPreview?.setImageBitmap(BitmapFactory.decodeFile(myFile2.path))
                binding?.imgPreviewClothes?.setImageBitmap(BitmapFactory.decodeFile(myFile1.path))
            }
        }

        binding?.btnUsePicture?.setOnClickListener {
            uploadImage()
            val resultFragment = ResultFragment()
            val fragmenManager = parentFragmentManager
            fragmenManager.beginTransaction().apply {
                replace(R.id.resultFragment, resultFragment, ResultFragment::class.java.simpleName)
                addToBackStack(null)
                commit()
            }
        }
        if(!allPermissionGranted()){
            requestPermission()
        }
        binding?.btnOpenCamera?.setOnClickListener {
            launchCameraX()
        }
        binding?.btnOpenCamera2?.setOnClickListener {
            launchCameraX()
        }
        binding?.btnOpenGallery?.setOnClickListener {
            launchGallery()
        }
        binding?.btnOpenGallery2?.setOnClickListener {
            launchGallery()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun allPermissionGranted() = REQUIRED_PERMISSION.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun launchCameraX() {
        val intent = Intent(requireActivity(), CameraActivity::class.java)
        if (binding?.btnOpenCamera?.isPressed == true){
            launcherIntentCameraX.launch(intent)
        } else if (binding?.btnOpenCamera2?.isPressed == true){
            launcherIntentCameraX2.launch(intent)
        }

    }

    private fun launchGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"

        val chooserIntent = Intent.createChooser(intent, resources.getString(R.string.choose_picture))
        if (binding?.btnOpenGallery?.isPressed == true){
            launcherIntentGallery.launch(chooserIntent)
        } else if(binding?.btnOpenGallery2?.isPressed == true){
            launcherIntentGallery2.launch(chooserIntent)
        }

    }

    private fun checkPermission(permission: String) : Boolean{
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            permission
        )==PackageManager.PERMISSION_GRANTED
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(file1 != null && file2 != null){
            outState.putByteArray("myFile", fileToByteArray(file1 as File))
            outState.putByteArray("myFile", fileToByteArray(file2 as File))
        }
    }

    private fun uploadImage() {
        if (file1 != null) {
            val myFile1 = file1 as File
            val compressedFile = reduceFileImage(myFile1)
            val requestImageFile = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())

            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                myFile1.name,
                requestImageFile
            )

            if (file2 != null){
                val myFile2 = file2 as File
                val compressedFile2 = reduceFileImage(myFile2)
                val requestImageFile2 = compressedFile2.asRequestBody("image/jpeg".toMediaTypeOrNull())

                val imageMultipart2: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "photo",
                    myFile1.name,
                    requestImageFile2
                )

                val apiService = ApiConfig().getApiService()
                val uploadImageRequest = apiService.generateImage(imageMultipart,imageMultipart2)
                uploadImageRequest.enqueue(object : Callback<TryonResponse>{
                    override fun onResponse(
                        call: Call<TryonResponse>,
                        response: Response<TryonResponse>
                    ) {
                        if (response.isSuccessful){
                            val responseBody = response.body()
                            if (responseBody != null){
                                Toast.makeText(activity, "Berhasil Upload", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(activity, "Response Body Null", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<TryonResponse>, t: Throwable) {
                        Toast.makeText(activity, t.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        } else {
            Toast.makeText(activity, "Silakan masukkan berkas gambar terlebih dahulu.", Toast.LENGTH_SHORT).show()
        }
    }
}