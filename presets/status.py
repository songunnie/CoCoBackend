STATUS_CODE = {
    'SUCCESS': 200,
    'BAD_REQUEST': 400,
    'INVALID_TOKEN': 400,
    'INVALID_PARAM': 400,
    'UNAUTHORIZED_USER': 401,
    'FORBIDDEN_USER': 403,
    'DUPLICATED_USER': 409,
    'ALREADY_SENT_VERIFICATION_MAIL': 429
}

STATUS_MESSAGE = {
    'SUCCESS': 'Success',
    'BAD_REQUEST': 'Bad request',
    'INVALID_TOKEN': 'Invalid token',
    'INVALID_PARAM': lambda param: f'Invalid {param} param',
    'UNAUTHORIZED_USER': 'Unauthorized user',
    'FORBIDDEN_USER': 'Forbidden user',
    'DUPLICATED_USER': 'Duplicated user',
    'ALREADY_SENT_VERIFICATION_MAIL': 'Already sent verification mail'
}
