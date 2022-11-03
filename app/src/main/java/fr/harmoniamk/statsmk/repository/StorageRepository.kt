package fr.harmoniamk.statsmk.repository

import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import fr.harmoniamk.statsmk.model.firebase.PictureResponse
import fr.harmoniamk.statsmk.model.firebase.UploadPictureResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import javax.inject.Inject

interface StorageRepositoryInterface {
    fun uploadPicture(userId: String?, image: Uri): Flow<UploadPictureResponse>
    fun getPicture(userId: String?): Flow<PictureResponse>
}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(ApplicationComponent::class)
interface StorageRepositoryModule {
    @Binds
    fun bindRepository(impl: StorageRepository): StorageRepositoryInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class StorageRepository @Inject constructor() : StorageRepositoryInterface {
    private val storageRef = Firebase.storage.reference

    override fun uploadPicture(userId: String?, image: Uri) = callbackFlow {
        if (userId == null && isActive) offer(UploadPictureResponse.Error())
        userId?.let { id ->
            storageRef.child(id).putFile(image).addOnCompleteListener {
                val response = when (it.isSuccessful) {
                    true -> UploadPictureResponse.Success()
                    else -> UploadPictureResponse.Error()
                }
                if (isActive) offer(response)
            }
        }
        awaitClose {  }
    }

    override fun getPicture(userId: String?) = callbackFlow {
        if (userId == null && isActive) offer(PictureResponse.Error())
        userId?.let {
            storageRef.child(it).downloadUrl.addOnCompleteListener {
                val response = when (it.isSuccessful) {
                    true -> PictureResponse.Success(it.result.toString())
                    else -> PictureResponse.Error()
                }
                if (isActive) offer(response)
            }
        }
        awaitClose {  }
    }

}