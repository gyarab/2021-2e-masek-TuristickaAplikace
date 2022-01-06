package com.example.sightseer;

import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Size;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.FlannBasedMatcher;
import org.opencv.features2d.SIFT;
import org.opencv.imgproc.Imgproc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;

public class Compare {
    Mat jedna;
    Mat dva;
    public Compare(Mat jedna, Mat dva) {
        this.jedna = jedna;
        this.dva = dva;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public double compare() {
        //System.loadLibrary("C:\\Users\\Denis\\Downloads\\opencv\\build\\java\\x64\\opencv_java453.dll");
        Mat img1 = jedna;
        Mat img2 = dva;

        //Kontrola rovnosti

        int[] image1 = new int[3];
        image1[0] = img1.height();
        image1[1] = img1.width();
        image1[2] = img1.channels();
        int[] image2 = new int[3];
        image2[0] = img2.height();
        image2[1] = img2.width();
        image2[2] = img2.channels();

        if (image1[0] == image2[0] && image1[1] == image2[1] && image1[2] == image2[2]) {
            //System.out.println("The images have same size and channels");
            Mat img3 = new Mat();
            Core.subtract(img1, img2, img3);
            ArrayList<Mat> mv = new ArrayList<>();
            Core.split(img3, mv);
            if (Core.countNonZero(mv.get(0)) == 0 && Core.countNonZero(mv.get(1)) == 0 && Core.countNonZero(mv.get(2)) == 0) {
                //System.out.println("Images are completely equal");
                return 100;
            } else {
                //System.out.println("Images are not completely equal");
            }
        } else {
            //System.out.println("The images don't have the same size and/or channels");
        }

        //Kontrola podobnosti

        SIFT sift = SIFT.create();
        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
        MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
        Mat descriptors1 = new Mat();
        Mat descriptors2 = new Mat();
        sift.detectAndCompute(img1, new Mat(), keypoints1, descriptors1);
        sift.detectAndCompute(img2, new Mat(), keypoints2, descriptors2);
        ArrayList<MatOfDMatch> matches = new ArrayList<>();
        FlannBasedMatcher flann = new FlannBasedMatcher();
        flann.knnMatch(descriptors1, descriptors2, matches, 2);

        ArrayList<DMatch> good_points = new ArrayList<>();
        for (Iterator<MatOfDMatch> iterator = matches.iterator(); iterator.hasNext(); ) {
            MatOfDMatch matOfDMatch = (MatOfDMatch) iterator.next();
            if (matOfDMatch.toArray()[0].distance / matOfDMatch.toArray()[1].distance < 0.5) {
                good_points.add(matOfDMatch.toArray()[0]);
            }
        }
        MatOfDMatch good_points_mat = new MatOfDMatch();
        good_points_mat.fromList(good_points);
        //System.out.println(keypoints1.total());
        //System.out.println(good_points.size());
        double percentage;
        double good100 = good_points.size() * 100;
        if (keypoints1.total() > keypoints2.total()) {
            percentage = good100 / keypoints2.total();
        } else {
            percentage = good100 / keypoints1.total();
        }
        //System.out.println("Match percentage: " + round(percentage, 1) + "%");

        //System.out.println(matches.size());
        Mat similarity = new Mat();
        Features2d.drawMatches(img1, keypoints1, img2, keypoints2, good_points_mat, similarity);
        Mat similar = new Mat();
        Imgproc.resize(similarity, similar, new Size(similarity.cols() / 4f, similarity.rows() / 4f));

        //System.out.println(image1[0] + ", " + image1[1] + ", " + image1[2]);
        //System.out.println(image2[0] + ", " + image2[1] + ", " + image2[2]);
        return round(percentage, 1);
    }
}
