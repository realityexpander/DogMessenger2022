package com.realityexpander.dogs.view


import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
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
import com.klinker.android.send_message.*
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
    private var pictureBitmap: Bitmap? = null

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
                    pictureBitmap = resource
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

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_send_sms -> {
                sendSmsStarted = true
                (activity as MainActivity).checkSmsPermission() // send from our app, must be set as default SMS tho!
            }
            R.id.action_share -> {
                shareImageStarted = true
                (activity as MainActivity).checkWriteExternalPermission()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun checkOrSetDefaultSmsApp(): Boolean {

        if (!dataBinding.root.context.packageName
                .equals(Telephony.Sms.getDefaultSmsPackage(dataBinding.root.context))) {

            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(
                Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                dataBinding.root.context.packageName
            )
            startActivity(intent)
            return false

//            val roleManager = getSystemService(RoleManager::class.java)
//            val roleRequestIntent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
//            startActivityForResult(roleRequestIntent, 12)
//            return false
        }

        return true
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun onPermissionResult(permissionGranted: Boolean) {
        if (sendSmsStarted && permissionGranted) {
            sendSmsStarted = false

            if (checkOrSetDefaultSmsApp()) {

                context?.let {
                    val smsInfo = SmsInfo("",
                        text = "${currentDog?.dogBreed} bred for ${currentDog?.bredFor}",
                        subject = "",
                        imageUrl = currentDog?.imageUrl,
                        imageUri = pictureFile?.let {
                            FileProvider.getUriForFile(
                                dataBinding.root.context,
                                dataBinding.root.context.applicationContext.packageName + ".provider",
                                pictureFile!!
                            )
                        }
                    )

                    val dialogBinding = DataBindingUtil.inflate<SendSmsDialogBinding>(
                        LayoutInflater.from(it),
                        R.layout.send_sms_dialog,
                        null,
                        false
                    )

                    AlertDialog.Builder(it)
                        .setView(dialogBinding.root)
                        .setPositiveButton("Send SMS") { dialog, which ->
                            if (!dialogBinding.smsDestination.text.isNullOrEmpty()) {
                                smsInfo.to = dialogBinding.smsDestination.text.toString()
                                smsInfo.subject = "Dogs Yo!"
                                sendSms(smsInfo)
                            }
                        }
                        .setNegativeButton("Cancel") { dialog, which -> }
                        .show()

                    dialogBinding.smsInfo = smsInfo
                }
            }
        }

        if(shareImageStarted && permissionGranted) {
            shareImageStarted = false

            val sharingStyle = 1

            val imageUri = pictureFile?.let {
                FileProvider.getUriForFile(
                    dataBinding.root.context,
                    dataBinding.root.context.packageName + ".provider",
                    pictureFile!!
                )
            }

            when (sharingStyle) {
                // "Send" style - More selective for SMS apps
                1 -> {

                    // ACTION_SEND - lets user pick the app - *USE THIS ONE*
                    // ACTION_SENDTO - uses the default app and no user choice, note: often has no-op (unknown why)
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.data =
                        Uri.parse("smsto:")  // This ensures only SMS apps respond, but doesnt matter "to" or no "to"
                    intent.type =
                        "image/jpeg"  // type *must* match the image format, or it only sends the text & ignores the image
                    intent.putExtra(
                        "sms_body",
                        "${currentDog?.dogBreed} bred for ${currentDog?.bredFor}"
                    )
                    intent.putExtra("subject", "Check out this dog breed")
                    intent.putExtra(Intent.EXTRA_STREAM, imageUri)
                    if (intent.resolveActivity(dataBinding.root.context.packageManager) != null) {
                        startActivity(intent)
                    }
                }

                // "Share" style, type="*/*" is ok
                2 -> {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.data = Uri.parse("smsto:")
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Check out this dog breed")
                    intent.putExtra(
                        Intent.EXTRA_TEXT, "${currentDog?.dogBreed} bred for ${currentDog?.bredFor}"
                    )
                    val chooser = Intent.createChooser(intent, "Share Dog")

                    if (pictureFile == null) {
                        intent.type = "text/plain"
                    } else {
                        intent.type = "*/*"
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                        val imageUri = FileProvider.getUriForFile(
//                            dataBinding.root.context,
//                            dataBinding.root.context.applicationContext.packageName + ".provider",
//                            pictureFile!!
//                        )
                        intent.putExtra(Intent.EXTRA_STREAM, imageUri)

                        // Grant permissions
                        val resInfoList: List<ResolveInfo> = dataBinding.root.context.packageManager
                            .queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
                        for (resolveInfo in resInfoList) {
                            val packageName = resolveInfo.activityInfo.packageName
                            dataBinding.root.context.grantUriPermission(
                                packageName,
                                imageUri,
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        }
                    }
                    startActivity(chooser)
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun sendSms(smsInfo: SmsInfo) {
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 0, intent, 0)
        val sms = SmsManager.getDefault()
        sms.sendTextMessage(smsInfo.to, null, smsInfo.text, pi, null)


        smsInfo.imageUri?.let {

            Thread {
                val sendSettings = Settings()
                val settings = Settings()
                sendSettings.mmsc = settings.mmsc
                sendSettings.proxy = settings.proxy
                sendSettings.port = settings.port
                sendSettings.useSystemSending = true // must be set to true to send
                val transaction = Transaction(dataBinding.root.context, sendSettings)
                val message = Message(smsInfo.text, smsInfo.to)
                message.fromAddress = Utils.getMyPhoneNumber(dataBinding.root.context)
                message.save = true // must be true to send

                // message.messageUri = smsInfo.imageUri // does not work
                // This may help: https://stackoverflow.com/questions/61686704/android-sending-mms-programmatically-without-being-default-app
                // https://github.com/DrBrad/android-smsmms/network
                // https://github.com/DrBrad

                message.setImage(pictureBitmap)
//                message.setImage(BitmapFactory.decodeResource(dataBinding.root.context.resources, R.drawable.common_full_open_on_phone))
                transaction.sendNewMessage(message, Transaction.NO_THREAD_ID)
            }.start()
        }


    }

    private fun getSimNumber(context: Context): String {
        val telephonyManager = context.getSystemService(
            Context.TELEPHONY_SERVICE
        ) as TelephonyManager

        return if (ActivityCompat.checkSelfPermission(
                dataBinding.root.context,
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                dataBinding.root.context,
                Manifest.permission.READ_PHONE_NUMBERS
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                dataBinding.root.context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ""
        } else
            telephonyManager.line1Number
    }

}
