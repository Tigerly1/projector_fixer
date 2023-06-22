import 'dart:async';
import 'dart:ffi';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:camera/camera.dart';
import 'package:flutter/services.dart';
import 'package:image/image.dart' as img;
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';


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
  static const platform = MethodChannel('samples.flutter.dev/camera');
  late CameraImage latestImage;
  var isInitialized = false;
  final TextEditingController _ipController = TextEditingController();
  final TextEditingController _portController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _requestPermission();
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
    
    

    if (controller?.value.isInitialized == true) {
      controller?.startImageStream((simg) {
        latestImage = simg;
        isInitialized = true;
      }).catchError((e) {
        print('Error starting image stream: $e');
      });

      timer = Timer.periodic(Duration(milliseconds: 3000), (Timer t) async {
        if (controller!.value.isStreamingImages && isInitialized) {
          await sendLatestFrame(controller!);
        }
      });
    } else {
      print('Error initializing camera');
    }
  }).catchError((e) {
    print('Error initializing camera: $e');
  });
    }
  }
  _requestPermission() async {
    if (await Permission.storage.request().isGranted) {
      // Either the permission was already granted before or the user just granted it.
      print('Storage permission granted');
    }
  }

  Future<void> sendLatestFrame(CameraController cnt) async {
    print("WELL");
    await cnt.stopImageStream();
    String pathBase = '${(await getTemporaryDirectory()).path}/${DateTime.now().toIso8601String()}';
    var imagePath = '$pathBase.png';
    XFile image = await cnt!.takePicture();
    image.saveTo(imagePath!);
    await File(imagePath!).exists();

    platform.invokeMethod('sendFrame', imagePath);

    // Start the image stream again
    cnt.startImageStream((simg) {
      latestImage = simg;
      isInitialized = true;
    }).catchError((e) {
      print('Error restarting image stream: $e');
    });
  }

// Updated timer


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
