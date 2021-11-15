package lotto;

import lotto.consts.LottoNumberConst;
import lotto.consts.PriceConst;
import lotto.consts.WinningEnum;
import lotto.domain.*;
import lotto.exception.WrongLottoSizeException;
import lotto.exception.WrongPriceException;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("로또에 대한 테스트")
public class LottoTest {

    @Test
    @DisplayName("최소 금액보다 작은 금액을 입력했을 경우에 대한 테스트")
    void min_price_test() {
        assertThatExceptionOfType(WrongPriceException.class).isThrownBy(() -> new Price(PriceConst.LOTTO_PRICE - 1));
    }

    @Test
    @DisplayName("발급한 로또 개수가 올바른지에 대한 테스트")
    void lotto_size_test() {
        Price price = new Price(PriceConst.LOTTO_PRICE * 15);
        Lottos lottos = new Lottos(price);

        assertThat(lottos.getLottos())
                .isNotNull()
                .isNotEmpty()
                .doesNotContainNull();
        assertThat(lottos.getLottos().size()).isEqualTo(15);
    }

    @Test
    @DisplayName("발급한 로또 번호가 올바른지에 대한 테스트")
    void lotto_numbers_test() {
        Price price = new Price(PriceConst.LOTTO_PRICE);
        Lottos lottos = new Lottos(price);
        Lotto lotto = lottos.getLottos().get(0);
        List<Integer> lottoNumbers = lotto.getNumbers();

        assertThat(lottoNumbers)
                .isNotNull()
                .isNotEmpty()
                .doesNotContainNull()
                .doesNotHaveDuplicates()
                .doNotHave(new Condition<>(lottoNumber -> lottoNumber < LottoNumberConst.START_NUMBER, "시작 번호보다 작습니다."))
                .doNotHave(new Condition<>(lottoNumber -> lottoNumber > LottoNumberConst.END_NUMBER, "끝 번호보다 큽니다."));
        assertThat(lottoNumbers.size()).isEqualTo(LottoNumberConst.LOTTO_NUMBER_SIZE);
    }

    @Test
    @DisplayName("입력한 당첨 번호에 대한 테스트1")
    void winning_numbers_test_1() {
        assertThatNullPointerException().isThrownBy(() -> new Lotto(null));
    }

    @ParameterizedTest
    @MethodSource("winning_numbers_test_data_2")
    @DisplayName("입력한 당첨 번호에 대한 테스트2")
    void winning_numbers_test_2(List<Integer> numbers) {
        assertThatExceptionOfType(WrongLottoSizeException.class).isThrownBy(() -> new Lotto(numbers));
    }

    static Stream<Arguments> winning_numbers_test_data_2() {
        return Stream.of(
                Arguments.of(new ArrayList<>()),
                Arguments.of(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5))),
                Arguments.of(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 1))),
                Arguments.of(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7)))
        );
    }

    @ParameterizedTest
    @MethodSource("winning_numbers_test_data_3")
    @DisplayName("입력한 당첨 번호에 대한 테스트3")
    void winning_numbers_test_3(List<Integer> numbers) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Lotto(new ArrayList<>(numbers)));
    }

    static Stream<Arguments> winning_numbers_test_data_3() {
        return Stream.of(
                Arguments.of(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, null))),
                Arguments.of(new ArrayList<>(Arrays.asList(LottoNumberConst.START_NUMBER - 1, 2, 3, 4, 5, 6))),
                Arguments.of(new ArrayList<>(Arrays.asList(LottoNumberConst.END_NUMBER + 1, 2, 3, 4, 5, 6)))
        );
    }

    @Test
    @DisplayName("로또 당첨 테스트")
    void winning_lotto_test() {
        List<Lotto> lottoList = new ArrayList<>();
        lottoList.add(new Lotto(Arrays.asList(1, 2, 3, 4, 5, 6)));
        lottoList.add(new Lotto(Arrays.asList(1, 2, 3, 4, 45, 6)));
        lottoList.add(new Lotto(Arrays.asList(1, 2, 3, 44, 45, 6)));
        lottoList.add(new Lotto(Arrays.asList(1, 2, 3, 44, 45, 6)));
        lottoList.add(new Lotto(Arrays.asList(1, 2, 43, 44, 45, 6)));
        lottoList.add(new Lotto(Arrays.asList(1, 42, 43, 44, 45, 6)));
        Lottos lottos = new Lottos(lottoList);
        Lotto winningLotto = new Lotto(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6)));
        WinningStats winningStats = new WinningStats(lottos, winningLotto);

        Map<WinningEnum, Integer> winningStatsMap = winningStats.getWinningStats();
        assertThat(winningStatsMap.get(WinningEnum.FIRST)).isEqualTo(1);
        assertThat(winningStatsMap.get(WinningEnum.THIRD)).isEqualTo(1);
        assertThat(winningStatsMap.get(WinningEnum.FOURTH)).isEqualTo(2);
        assertThat(winningStatsMap.get(WinningEnum.FIFTH)).isEqualTo(1);
    }

    @Test
    @DisplayName("총 수익에 대한 테스트")
    void total_prize_test() {
        Price price = new Price(PriceConst.LOTTO_PRICE * 14);
        List<Lotto> lottoList = new ArrayList<>();
        lottoList.add(new Lotto(Arrays.asList(1, 2, 3, 43, 44, 45)));
        Lottos lottos = new Lottos(lottoList);
        Lotto winningLotto = new Lotto(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6)));
        WinningStats winningStats = new WinningStats(lottos, winningLotto);
        ProfitRate profitRate = price.getProfitRate(winningStats);

        assertThat(profitRate.getProfitRate()).isEqualTo(0.35);
    }
}
