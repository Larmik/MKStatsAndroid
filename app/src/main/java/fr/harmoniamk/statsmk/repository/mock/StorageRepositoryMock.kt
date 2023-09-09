package fr.harmoniamk.statsmk.repository.mock

import android.net.Uri
import fr.harmoniamk.statsmk.model.firebase.PictureResponse
import fr.harmoniamk.statsmk.model.firebase.UploadPictureResponse
import fr.harmoniamk.statsmk.repository.StorageRepositoryInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class StorageRepositoryMock : StorageRepositoryInterface {
    override fun uploadPicture(userId: String?, image: Uri): Flow<UploadPictureResponse> = flow {
        emit(UploadPictureResponse.Success())
    }

    override fun getPicture(userId: String?): Flow<PictureResponse> = flow {
        emit(PictureResponse.Success(""))
    }
}