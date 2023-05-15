import 'dart:async';
import 'package:flutter/material.dart';
import 'package:camera/camera.dart';
import 'package:flutter/services.dart';

late List<CameraDescription> cameras;

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  cameras = await availableCameras();
  runApp(CameraApp());
}

class CameraApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Projection fixer',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: CameraScreen(cameras),
    );
  }
}

class CameraScreen extends StatefulWidget {
  final List<CameraDescription> cameras;

  const CameraScreen(this.cameras);

  @override
  CameraScreenState createState() {
    return CameraScreenState();
  }
}

class CameraScreenState extends State<CameraScreen> {
  CameraController? controller;
  Timer? timer;
  static const platform = const MethodChannel('samples.flutter.dev/camera');
  CameraImage? latestImage;

  final TextEditingController _ipController = TextEditingController();
  final TextEditingController _portController = TextEditingController();

  @override
  void initState() {
    super.initState();
     WidgetsBinding.instance.addPostFrameCallback((_) {
      _showIPAndPortDialog();
    });

    if (widget.cameras.isEmpty) {
      print('No camera available');
    } else {
      controller = CameraController(widget.cameras[0], ResolutionPreset.medium);

      controller?.initialize().then((_) {
        if (!mounted) {
          return;
        }
        setState(() {});

        // Start the image stream and setup the frame sending timer
        controller?.startImageStream((CameraImage img) {
          // Nothing to do here, the frames are handled by the timer
        });
            print("nie ok");

        timer = Timer.periodic(Duration(milliseconds: 200), (Timer t) {
          // Send the latest frame
                      print("ok");

          if (controller!.value.isStreamingImages) {
            sendLatestFrame();
          }
        });
      });
    }
  }

  Future<void> sendLatestFrame() async {
    // Send the frame data through the platform channel
    print("kurwa");
    print(latestImage?.planes[0]);
    await platform.invokeMethod('sendFrame', latestImage?.planes[0].bytes);
    // Reset the latest image
    latestImage = null;
  }

  Future<void> _showIPAndPortDialog() async {
    return showDialog<void>(
      context: context,
      barrierDismissible: false, // user must tap button!
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text('Enter IP and Port'),
          content: SingleChildScrollView(
            child: ListBody(
              children: <Widget>[
                TextFormField(
                  controller: _ipController,
                  decoration: InputDecoration(
                    hintText: 'Enter IP',
                  ),
                ),
                TextFormField(
                  controller: _portController,
                  decoration: InputDecoration(
                    hintText: 'Enter Port',
                  ),
                ),
              ],
            ),
          ),
          actions: <Widget>[
            TextButton(
              child: Text('Submit'),
              onPressed: () {
                Navigator.of(context).pop();
                // Here you can use _ipController.text and _portController.text to get IP and Port values
              },
            ),
          ],
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    if (controller == null || !controller!.value.isInitialized) {
      return Container(
        decoration: const BoxDecoration(
          image: DecorationImage(
            image: AssetImage('assets/images/wall.png'),
            fit: BoxFit.fill,
          ),
        ),
      );
    }
    return AspectRatio(
      aspectRatio: controller!.value.aspectRatio,
      child: CameraPreview(controller!),
    );
  }

  @override
  void dispose() {
    timer?.cancel();
    controller?.dispose();
    super.dispose();
  }
}
