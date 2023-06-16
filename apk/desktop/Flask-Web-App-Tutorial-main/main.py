from website import create_app
from flask import request
import json

app = create_app()


@app.route("/params", methods=['POST'])
def load_params():
    req_json = request.get_json()
    with open('static/params.json', 'w') as f:
        json.dump(req_json, f)
    return


if __name__ == '__main__':
    app.run(debug=True)
