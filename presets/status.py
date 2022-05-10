STATUS_CODE = {
    'SUCCESS': 200,
    'BAD_REQUEST': 400,
    'INVALID_TOKEN': 400,
    'INVALID_PARAM': 400,
    'FORBIDDEN_USER': 403
}

STATUS_MESSAGE = {
    'SUCCESS': 'Success',
    'BAD_REQUEST': 'Bad request',
    'INVALID_TOKEN': 'Invalid token',
    'INVALID_PARAM': lambda param: f'Invalid {param} param',
    'FORBIDDEN_USER': 'Forbidden user'
}
