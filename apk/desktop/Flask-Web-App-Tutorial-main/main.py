from website import create_app
from flask import request
import json

app = create_app()


@app.route("/params", methods=['POST'])
def load_params():
    req_json = request.get_json()
    with open('website/static/params.json', 'w') as f:
        json.dump(req_json, f)
    return '', 200


if __name__ == '__main__':
    app.run(debug=True)
