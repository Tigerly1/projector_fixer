class ParamsSaver:
    _instance = None

    def __new__(cls, *args, **kwargs):
        if not cls._instance:
            cls._instance = super(ParamsSaver, cls).__new__(cls, *args, **kwargs)
        return cls._instance

    def __init__(self):
        self._value = None

    def set_value(self, value):
        self._value = value

    def get_value(self):
        return self._value