STATUS_CODE = {
    'SUCCESS': 200,
    'INVALID_PARAM': 400
}

STATUS_MESSAGE = {
    'SUCCESS': 'Success',
    'INVALID_PARAM': lambda param: f'Invalid {param} param',
}
