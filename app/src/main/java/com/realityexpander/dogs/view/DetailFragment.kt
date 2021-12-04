package com.realityexpander.dogs.view


import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.telephony.SmsManager
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.realityexpander.dogs.R
import com.realityexpander.dogs.databinding.FragmentDetailBinding
import com.realityexpander.dogs.databinding.SendSmsDialogBinding
import com.realityexpander.dogs.model.DogBreed
import com.realityexpander.dogs.model.DogPalette
import com.realityexpander.dogs.model.SmsInfo
import com.realityexpander.dogs.viewmodel.DetailViewModel
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

private const val TAG = "DetailFragment"

class DetailFragment : Fragment() {

    private lateinit var viewModel: DetailViewModel
    private var dogUuid = 0

    private lateinit var dataBinding: FragmentDetailBinding
    private var sendSmsStarted = false
    private var shareImageStarted = false
    private var currentDog: DogBreed? = null
    private var pictureFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            dogUuid = DetailFragmentArgs.fromBundle(it).dogUuid
        }

        viewModel = ViewModelProviders.of(this).get(DetailViewModel::class.java)

        observeViewModel()
        viewModel.fetch(dogUuid)
    }

    private fun observeViewModel() {
        viewModel.dogLiveData.observe(this, Observer { dog ->
            currentDog = dog
            dog?.let {
                dataBinding.dog = dog

                it.imageUrl?.let {
                    setupBackgroundColor(it)
                }
            }
        })
    }

    private fun setupBackgroundColor(url: String) {
        Glide.with(this)
            .asBitmap()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    storeImage(resource)
                    Palette.from(resource)
                        .generate { palette ->
                            val intColor = palette?.lightMutedSwatch?.rgb ?: 0
                            val myPalette = DogPalette(intColor)
                            dataBinding.palette = myPalette
                        }
                }

            })
    }

    private fun storeImage(image: Bitmap) {
        pictureFile = getOutputMediaFile()
        if (pictureFile == null) {
            Log.d(
                TAG,
                "Error creating media file, check storage permissions: "
            ) // e.getMessage());

            return
        }
        try {
            val fos = FileOutputStream(pictureFile)
            image.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.close()
            Log.d(TAG, "img dir: $pictureFile")
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "File not found: " + e.message)
        } catch (e: IOException) {
            Log.d(TAG, "Error accessing file: " + e.message)
        }
    }

    private fun getOutputMediaFile(): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Path")
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }
//        val mediaFile: File
//        val generator = Random()
//        var n = 1000
//        n = generator.nextInt(n)
//        val mImageName = "Image-$n.jpeg"
//        mediaFile = File(mediaStorageDir.path + File.separator.toString() + mImageName)
//        return mediaFile
        return File(mediaStorageDir.path + File.separator.toString() + "temp_sharing_file.jpeg" )
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.detail_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_send_sms -> {
                sendSmsStarted = true
                (activity as MainActivity).checkSmsPermission()
            }
            R.id.action_share -> {
                shareImageStarted = true
                (activity as MainActivity).checkWriteExternalPermission()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun onPermissionResult(permissionGranted: Boolean) {
        if (sendSmsStarted && permissionGranted) {
            sendSmsStarted = false
            context?.let {
                val smsInfo =
                    SmsInfo("", "${currentDog?.dogBreed} bred for ${currentDog?.bredFor}", currentDog?.imageUrl)

                val dialogBinding = DataBindingUtil.inflate<SendSmsDialogBinding>(
                    LayoutInflater.from(it),
                    R.layout.send_sms_dialog,
                    null,
                    false
                )

                AlertDialog.Builder(it)
                    .setView(dialogBinding.root)
                    .setPositiveButton("Send SMS") {dialog, which ->
                        if(!dialogBinding.smsDestination.text.isNullOrEmpty()) {
                            smsInfo.to = dialogBinding.smsDestination.text.toString()
                            sendSms(smsInfo)
                        }
                    }
                    .setNegativeButton("Cancel") {dialog, which -> }
                    .show()

                dialogBinding.smsInfo = smsInfo
            }
        }

        if(shareImageStarted && permissionGranted) {
            shareImageStarted = false
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_SUBJECT, "Check out this dog breed")
            intent.putExtra(Intent.EXTRA_TEXT, "${currentDog?.dogBreed} bred for ${currentDog?.bredFor}")
            if (pictureFile == null) {
                intent.type = "text/plain"
            } else {
                intent.type = "*/*"
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.putExtra(Intent.EXTRA_STREAM,
                    FileProvider.getUriForFile(dataBinding.root.context,
                        dataBinding.root.context.applicationContext.packageName + ".provider",
                        pictureFile!!)
                )
            }
            startActivity(Intent.createChooser(intent, "Share with"))
        }
    }

    private fun sendSms(smsInfo: SmsInfo) {
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 0, intent, 0)
        val sms = SmsManager.getDefault()
        sms.sendTextMessage(smsInfo.to, null, smsInfo.text, pi, null)
    }

}
