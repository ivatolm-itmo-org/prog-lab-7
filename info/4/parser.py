class BaseReader:
    def open(self) -> None:
        raise NotImplementedError
    def close(self) -> None:
        raise NotImplementedError
    def consume(self) -> str:
        raise NotImplementedError
    def peek(self) -> str:
        raise NotImplementedError

class SeqReader(BaseReader):
    def __init__(self):
        self.file_stream = None
        self.mem = None

    def open(self, file):
        if self.file_stream is not None:
            self.close()
        self.file_stream = open(file, 'r')
        self._read()

    def close(self):
        if self.file_stream is not None:
            self.file_stream.close()
            self.mem = None

    def consume(self):
        char = self.mem
        self._read()
        return char

    def peek(self):
        return self.mem

    def _read(self):
        self.mem = self.file_stream.read(1)
        return self.mem


class Lexer:
    def __init__(self, reader, tokens):
        self.reader = reader
        self.tokens = tokens

    def consume(self):
        return self._read()

    def peek(self):
        pass

    def _read(self):
        seq = ''
        while True:
            char = self.reader.consume()
            seq.append(char)
            if seq in self.tokens:
                self.mem = self.tokens[seq]
                return self.mem


reader = SeqReader()
reader.open("parser.py")

json_tokens = ['"', ':']
lexer = Lexer(reader, tokens)
print(lexer.consume())

