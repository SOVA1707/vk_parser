package ru.scam.parser.calc;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import ru.scam.parser.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tool {
    public static void loadLibraries() {
        try {
            String osName = System.getProperty("os.name");
            String opencvpath = System.getProperty("user.dir");
            if (osName.startsWith("Windows")) {
                int bitness = Integer.parseInt(System.getProperty("sun.arch.data.model"));
                if (bitness == 32) {
                    opencvpath = opencvpath + "\\opencv\\x86\\";
                } else if (bitness == 64) {
                    opencvpath = opencvpath + "\\opencv\\x64\\";
                } else {
                    opencvpath = opencvpath + "\\opencv\\x86\\";
                }
            } else if (osName.equals("Mac OS X")) {
                opencvpath = opencvpath + "Your path to .dylib";
            }
            System.load(opencvpath + Core.NATIVE_LIBRARY_NAME + ".dll");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load opencv native library", e);
        }
    }


    public static String getEquationFromImage(String filename) {
        Mat img = Imgcodecs.imread(main.FARM_PATH + "1.jpg", Imgcodecs.IMREAD_GRAYSCALE);
        Mat img2 = Imgcodecs.imread(filename, Imgcodecs.IMREAD_GRAYSCALE);
        img2 = img2.submat(220, 310, 80, 630);
        List<Mat> letters;
        {
            Mat l1 = img.submat(26, 89, 41, 58).clone();
            Mat l2 = img.submat(22, 88, 85, 123).clone();
            Mat l3 = img.submat(26, 89, 144, 184).clone();
            Mat l4 = img.submat(23, 86, 206, 253).clone();
            Mat l5 = img.submat(25, 89, 272, 316).clone();
            Mat l6 = img.submat(24, 88, 336, 377).clone();
            Mat l7 = img.submat(26, 88, 401, 441).clone();
            Mat l8 = img.submat(21, 85, 472, 514).clone();
            Mat l9 = img.submat(20, 84, 528, 571).clone();
            Mat l0 = img.submat(105, 171, 393, 447).clone();
            Mat lplus = img.submat(122, 169, 24, 71).clone();
            Mat lminus = img.submat(140, 150, 80, 120).clone();
            Mat lmul = img.submat(103, 129, 145, 173).clone();
            Mat ldel = img.submat(98, 173, 207, 241).clone();
            letters = new ArrayList<>(Arrays.asList(l1, l2, l3, l4, l5, l6, l7, l8, l9, l0, lplus, lminus, lmul, ldel));
        }
        List<Mat> example = new ArrayList<>();

        int start = 0;
        int end;
        for (int i = 0; i < img2.cols(); i++) {
            boolean check = false;
            for (int j = 0; j < img2.rows(); j++) {
                if (img2.get(j,i)[0] < 220) {
                    check = true;
                    break;
                }
            }
            if (check) {
                if (start == 0) {
                    start = i-10;
                }
            }else {
                if (start != 0) {
                    end = i+10;
                    example.add(img2.submat(0,img2.rows(),start, end).clone());
                    start = 0;
                }
            }
        }
        StringBuilder text = new StringBuilder();
        for (Mat mat : example) {
            for (int i = 0; i < letters.size(); i++) {
                Mat letter = letters.get(i);
                Mat result = new Mat();
                try {
                    Imgproc.matchTemplate(mat, letter, result, Imgproc.TM_SQDIFF);
                    Core.MinMaxLocResult r = Core.minMaxLoc(result);
                    if (!(r.minVal > 1.0E6)) {
                        String s;
                        switch (i) {
                            case 0: {
                                s = "1";
                                break;
                            }
                            case 1: {
                                s = "2";
                                break;
                            }
                            case 2: {
                                s = "3";
                                break;
                            }
                            case 3: {
                                s = "4";
                                break;
                            }
                            case 4: {
                                s = "5";
                                break;
                            }
                            case 5: {
                                s = "6";
                                break;
                            }
                            case 6: {
                                s = "7";
                                break;
                            }
                            case 7: {
                                s = "8";
                                break;
                            }
                            case 8: {
                                s = "9";
                                break;
                            }
                            case 9: {
                                s = "0";
                                break;
                            }
                            case 10: {
                                s = "+";
                                break;
                            }
                            case 11: {
                                s = "-";
                                break;
                            }
                            case 12: {
                                s = "*";
                                break;
                            }
                            case 13: {
                                s = "/";
                                break;
                            }
                            case 14: {
                                s = "=";
                                break;
                            }
                            case 15: {
                                s = "?";
                                break;
                            }
                            default: {
                                s = "";
                                System.out.println("...................S is empty....................");
                            }
                        }
                        text.append(s);
                        break;
                    }
                }catch (Exception ignored) {}
            }
        }

        img.release();
        img2.release();
        letters.forEach(Mat::release);

        System.out.println(text);
        Calc.Calculate(String.valueOf(text));

        return Calc.popVal();
    }
}
