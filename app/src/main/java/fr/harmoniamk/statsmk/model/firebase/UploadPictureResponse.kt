package fr.harmoniamk.statsmk.model.firebase

sealed class UploadPictureResponse {
    class Success : UploadPictureResponse()
    class Error: UploadPictureResponse()
}

sealed class PictureResponse {
    class Success : PictureResponse()
    class Error: PictureResponse()
}