from website import create_app
from flask import request
import json

from website.params import ParamsSaver

app = create_app()


@app.route("/params", methods=['POST'])
def load_params():
    data = json.loads(request.data)
    print(data)
    param_saver = ParamsSaver()
    param_saver.set_value(data['data'])
    return "", 200


if __name__ == '__main__':
    app.run(debug=True)
