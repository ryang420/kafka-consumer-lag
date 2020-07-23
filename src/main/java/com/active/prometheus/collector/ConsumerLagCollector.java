package com.active.prometheus.collector;

import com.active.kafka.KafkaConsumerGroupService;
import com.active.kafka.PartitionAssignmentState;
import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Slf4j
public class ConsumerLagCollector extends Collector {
    private Properties properties;

    public ConsumerLagCollector(Properties properties) {
        this.properties = properties;
    }


    @Override
    public List<Collector.MetricFamilySamples> collect() {
        List<Collector.MetricFamilySamples> mfs = new ArrayList<>();

        // With labels
        GaugeMetricFamily labeledGauge = new GaugeMetricFamily("kafka_consumergroup_lag",
                "kafka consumer group lag",
                Arrays.asList("consumergroup", "partition", "topic"));

        GaugeMetricFamily currentOffsetGauge = new GaugeMetricFamily("kafka_consumergroup_current_offset",
                "kafka consumer group current offset",
                Arrays.asList("consumergroup", "partition", "topic"));

        KafkaConsumerGroupService service = new KafkaConsumerGroupService(properties);
        service.init();

        List<PartitionAssignmentState> consumerGroupList = service.collectAllGroupsAssignment();
        for (PartitionAssignmentState partitionAssignmentState : consumerGroupList) {
            // export consumer lag metrics
            labeledGauge.addMetric(Arrays.asList(partitionAssignmentState.getGroup(),
                    partitionAssignmentState.getPartition() + "",
                    partitionAssignmentState.getTopic()), partitionAssignmentState.getLag());

            // export current offset metrics
            currentOffsetGauge.addMetric(Arrays.asList(partitionAssignmentState.getGroup(),
                    partitionAssignmentState.getPartition() + "",
                    partitionAssignmentState.getTopic()), partitionAssignmentState.getOffset());
        }


        mfs.add(labeledGauge);
        mfs.add(currentOffsetGauge);

        service.close();

        return mfs;

    }
}
