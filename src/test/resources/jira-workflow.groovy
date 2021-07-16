when 'To Do', {
    'success' should: 'Passed'
}

when 'PASSED', {
    'success' should: 'Passed'
}

when 'FAILED', {
    'success' should: 'Passed'
}

when 'BLOCKED', {
    'success' should: 'Passed'
}

when 'To Do', {
    'failure' should: 'FAILED'
}

when 'PASSED', {
    'failure' should: 'FAILED'
}

when 'FAILED', {
    'failure' should: 'FAILED'
}

when 'BLOCKED', {
    'failure' should: 'FAILED'
}
