import re

# Grammar
QUOTE = r'"'
COLON = r':'
COMMA = r','
LEFTBRACE, RIGHTBRACE = r'{}'
LEFTBRACKET, RIGHTBRACKET = r'[]'
TRUE, FALSE = r'true', r'false'
NULL = r'null'

WHITESPACE = [' ', '\t', '\n']
SYNTAX = [COMMA, COLON, LEFTBRACKET, RIGHTBRACKET,
          LEFTBRACE, RIGHTBRACE]

DIGITS = "0123456789"

# Regular expressions
re_string = re.compile(r'"[^"]*"')
re_int = re.compile(r'\d+')
re_bool = re.compile(r'(true)|(false)')
re_null = re.compile(r'(null)')


def lex_string(string):
    if string[0] != QUOTE:
        return None, string

    json_string = ""
    for c in string[1:]:
        if c == QUOTE:
            break
        json_string += c
    else:
        raise Exception("Lexer error: closing quote expected")

    return json_string, string[1+len(json_string)+1:]


def lex_number_int(string):
    if string[0] not in DIGITS:
        return None, string

    json_number = ""
    for c in string:
        if c not in DIGITS:
            break
        json_number += c
    return int(json_number), string[len(json_number):]


def lex_bool(string):
    if string[:len(TRUE)] == TRUE:
        return True, string[len(TRUE):]

    elif string[:len(FALSE)] == FALSE:
        return False, string[len(FALSE):]

    return None, string


def lex_null(string):
    if string[:len(NULL)] == NULL:
        return True, string[len(NULL):]

    return None, string


def lex_regex_string(string):
    if string[0] != QUOTE:
        return None, string

    match = re_string.search(string)[0]
    return match[1:-1], string[len(match):]


def lex_regex_number_int(string):
    if string[0] not in DIGITS:
        return None, string

    match = re_int.search(string)[0]
    return int(match), string[len(match):]


def lex_regex_bool(string):
    if string[0] not in [TRUE[0], FALSE[0]]:
        return None, string

    match = re_bool.search(string)[0]
    return bool(match), string[len(match):]


def lex_regex_null(string):
    if string[0] not in [NULL[0]]:
        return None, string

    match = re_null.search(string)[0]
    return True, string[len(match):]


def lex(string, regex=False):
    tokens = []

    while len(string):
        if not regex:
            json_string, string = lex_string(string)
        else:
            json_string, string = lex_regex_string(string)
        if json_string is not None:
            tokens.append(json_string)
            continue

        if not regex:
            json_number, string = lex_number_int(string)
        else:
            json_number, string = lex_regex_number_int(string)
        if json_number is not None:
            tokens.append(json_number)
            continue

        if not regex:
            json_bool, string = lex_bool(string)
        else:
            json_bool, string = lex_regex_bool(string)
        if json_bool is not None:
            tokens.append(json_bool)
            continue

        if not regex:
            json_null, string = lex_regex_null(string)
        else:
            json_null, string = lex_null(string)
        if json_null is not None:
            tokens.append(None)
            continue

        if string[0] in WHITESPACE:
            string = string[1:]
        elif string[0] in SYNTAX:
            tokens.append(string[0])
            string = string[1:]
        else:
            raise Exception(f"Lexer error: unknown character {string[0]}")

    return tokens


def parse_array(tokens):
    json_array = []

    if tokens[0] == RIGHTBRACKET:
        return json_array, tokens[1:]

    while True:
        value, tokens = parse(tokens)

        json_array.append(value)

        if tokens[0] == RIGHTBRACKET:
            return json_array, tokens[1:]
        elif tokens[0] != COMMA:
            raise Exception("Parse error: comma expected")

        tokens = tokens[1:]


def parse_object(tokens):
    json_object = {}

    if tokens[0] == RIGHTBRACE:
        return json_object, tokens[1:]

    while True:
        key = tokens[0]
        if not isinstance(key, str):
            raise Exception("Parser error: key expected")

        colon = tokens[1]
        if colon != COLON:
            raise Exception("Parse error: colon expected")

        value, tokens = parse(tokens[2:])

        json_object[key] = value

        if tokens[0] == RIGHTBRACE:
            return json_object, tokens[1:]
        elif tokens[0] != COMMA:
            raise Exception("Parse error: comma expected")

        tokens = tokens[1:]


def parse(tokens):
    t = tokens[0]

    if t == LEFTBRACKET:
        return parse_array(tokens[1:])
    elif t == LEFTBRACE:
        return parse_object(tokens[1:])
    else:
        return t, tokens[1:]
