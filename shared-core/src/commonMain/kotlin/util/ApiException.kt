package util

class ApiException(val apiError: ApiError) : Exception(apiError.message)
