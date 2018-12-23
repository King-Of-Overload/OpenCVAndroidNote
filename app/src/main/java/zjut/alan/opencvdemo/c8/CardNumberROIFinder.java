package zjut.alan.opencvdemo.c8;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class CardNumberROIFinder {

    public static Bitmap extractNumberROI(Bitmap input, Bitmap template) {
        Mat src = new Mat();
        Mat tpl = new Mat();
        Mat dst = new Mat();
        Mat fixSrc = new Mat();
        Utils.bitmapToMat(input, src);
        Utils.bitmapToMat(template, tpl);
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGRA2GRAY);
        //边缘检测
        Imgproc.Canny(dst, dst, 200, 400,3, false);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierachy = new Mat();
        //寻找轮廓
        Imgproc.findContours(dst, contours, hierachy, Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.cvtColor(dst,dst,Imgproc.COLOR_GRAY2BGR);
        int width = input.getWidth();
        int height = input.getHeight();
        Rect roiArea = null;
        for(int i = 0; i < contours.size(); i++){
            List<Point> points = contours.get(i).toList();
            Rect rect = Imgproc.boundingRect(contours.get(i));
            if(rect.width < width && rect.width > (width / 2)){
                if(rect.height <= (height / 4)){
                    continue;
                }
                roiArea = rect;
            }
        }
        //裁剪ROI 区域
        Mat result = src.submat(roiArea);
        //修正大小，匹配模板
        Size fixSize = new Size(547, 342);
        Imgproc.resize(result, fixSrc, fixSize);
        result = fixSrc;
        //检测区域
        int result_cols = result.cols() - tpl.cols() + 1;
        int result_rows = result.rows() - tpl.rows() + 1;
        Mat mr = new Mat(result_rows, result_cols, CvType.CV_32FC1);

        //模板匹配
        Imgproc.matchTemplate(result, tpl, mr, Imgproc.TM_CCORR_NORMED);
        Core.normalize(mr,mr,0,1,Core.NORM_MINMAX, -1);
        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(mr);
        Point maxLoc = minMaxLocResult.maxLoc;
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        //find id number ROI
        Rect idNumberROI = new Rect((int)(maxLoc.x+tpl.cols()),(int)maxLoc.y,
                (int)(result.cols() - (maxLoc.x+tpl.cols())-40),tpl.rows()-10);
        Mat idNumberArea = result.submat(idNumberROI);
        //返回对象
        Bitmap bmp = Bitmap.createBitmap(idNumberArea.cols(),idNumberArea.rows(),conf);
        Utils.matToBitmap(idNumberArea, bmp);
        //释放内存
        idNumberArea.release();
        result.release();
        fixSrc.release();
        src.release();
        dst.release();
        return bmp;
    }


}
