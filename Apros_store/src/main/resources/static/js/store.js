'use strict';

// ========================================
// 1. 商品画像の読み込みエラー対応
// ========================================

document.querySelectorAll('[data-product-image]').forEach(img => {

    const fallback = () => {

        if (img.dataset.fallback && !img.dataset.failed) {
            img.dataset.failed = 'true';
            img.src = img.dataset.fallback;
        }
    };

    img.addEventListener('error', fallback);

    // JavaScript実行前に画像の読み込みが失敗した場合
    if (img.complete && img.naturalWidth === 0) {
        fallback();
    }
});


// ========================================
// 2. パスワードの表示・非表示
// ========================================

document.querySelectorAll('.password-toggle').forEach(button => {

    button.addEventListener('click', () => {

        const input = document.getElementById(
            button.getAttribute('aria-controls')
        );

        if (!input) return;

        const show = input.type === 'password';

        input.type = show ? 'text' : 'password';
        button.textContent = show ? '非表示' : '表示';
        button.setAttribute('aria-pressed', String(show));
    });
});


// ========================================
// 3. フォームの二重送信防止
// ========================================

document.querySelectorAll('form[data-submit-once]').forEach(form => {

    form.addEventListener('submit', event => {

        if (form.dataset.submitting === 'true') {
            event.preventDefault();
            return;
        }

        form.dataset.submitting = 'true';
        form.setAttribute('aria-busy', 'true');
    });
});

// 戻るボタンなどで再表示された場合は送信状態を解除
window.addEventListener('pageshow', () => {

    document.querySelectorAll('form[data-submit-once]').forEach(form => {

        delete form.dataset.submitting;
        form.removeAttribute('aria-busy');
    });
});


// ========================================
// 4. 商品一覧・検索・カテゴリー・並び替え
// ========================================

const grid = document.getElementById('product-grid');

if (grid) {

    const cards = Array.from(
        grid.querySelectorAll('.product-card')
    );

    const keyword = document.getElementById('keyword');
    const sort = document.getElementById('sort');
    const resultCount = document.getElementById('result-count');
    const noResults = document.getElementById('no-results');

    let category = 'all';

    // 検索用の文字列を統一
    const normalize = str =>
        String(str).normalize('NFKC').toLocaleLowerCase('ja');

    // カテゴリーIDから分類を決定
    const categoryFor = id => {

        switch (id) {
            case 1:
                return 'digital';

            case 2:
                return 'fashion';

            case 3:
                return 'food';

            case 4:
                return 'book';

            default:
                return null;
        }
    };

    // 各商品カードにカテゴリーと初期位置を保存
    cards.forEach((card, index) => {

        card.dataset.position = String(index);

        const categoryId = Number(card.dataset.categoryId);

        const group = categoryFor(categoryId);

        if (group !== null) {
            card.dataset.group = group;
        }
    });


    // ----------------------------------------
    // 商品一覧を更新
    // ----------------------------------------

    function render() {

        const words = normalize(keyword.value)
            .trim()
            .split(/\s+/)
            .filter(Boolean);

        let count = 0;

        // 商品の並び替え
        const ordered = cards.slice().sort((a, b) => {

            if (sort.value === 'low') {
                return Number(a.dataset.price)
                     - Number(b.dataset.price);
            }

            if (sort.value === 'high') {
                return Number(b.dataset.price)
                     - Number(a.dataset.price);
            }

            if (sort.value === 'name') {
                return a.dataset.name.localeCompare(
                    b.dataset.name, 'ja'
                );
            }

            return Number(a.dataset.position)
                 - Number(b.dataset.position);
        });

        // 検索条件とカテゴリー条件で絞り込み
        ordered.forEach(card => {

            const matchesCategory =
                category === 'all' ||
                category === card.dataset.group;

            const matchesKeyword = words.every(word =>
                normalize(card.dataset.name).includes(word)
            );

            const matches = matchesCategory && matchesKeyword;

            card.hidden = !matches;

            if (matches) {
                count++;
            }

            grid.append(card);
        });

        // 商品件数を表示
        if (resultCount) {
            resultCount.textContent = `${count} 件の商品`;
        }

        // 検索結果が0件の場合
        if (noResults) {
            noResults.hidden = count > 0;
        }
    }


    // ----------------------------------------
    // 検索・並び替え・カテゴリー切り替え
    // ----------------------------------------

    const searchForm = document.getElementById('product-search');

    if (searchForm) {
        searchForm.addEventListener('submit', event => {
            event.preventDefault();
            render();
        });
    }

    keyword.addEventListener('input', render);

    sort.addEventListener('change', render);

    const tabs = document.querySelectorAll('[data-category]');

    tabs.forEach(button => {

        button.addEventListener('click', () => {

            category = button.dataset.category;

            tabs.forEach(tab => {

                const active = tab === button;

                tab.classList.toggle('active', active);
                tab.setAttribute('aria-pressed', String(active));
            });

            render();
        });
    });


    // ----------------------------------------
    // 検索条件のリセット
    // ----------------------------------------

    const resetButton = document.getElementById('reset-filters');

    if (resetButton) {

        resetButton.addEventListener('click', () => {

            keyword.value = '';
            sort.value = 'default';

            if (tabs.length > 0) {
                tabs[0].click();
            } else {
                category = 'all';
                render();
            }

            keyword.focus();
        });
    }

    // 初期表示
    render();
}


// ========================================
// 5. 配送先の切り替え
// ========================================

const shippingFields = document.getElementById('shipping-fields');

if (shippingFields) {

    const fields = Array.from(
        shippingFields.querySelectorAll('input')
    );

    const radios = Array.from(
        document.querySelectorAll('input[name="shippingType"]')
    );

    // 会員登録済みの住所情報
    const registeredAddress =
        document.getElementById('registered-address');

    // 現在HTMLに表示されている住所
    const initialAddress = Object.fromEntries(
        fields.map(input => [input.id, input.value])
    );

    // 登録済み住所を取得
    const registered = {};

    fields.forEach(input => {

        if (registeredAddress) {
            registered[input.id] =
                registeredAddress.dataset[input.id] ?? '';
        } else {
            registered[input.id] = initialAddress[input.id];
        }
    });

    // 現在選択されている配送先
    let previous = document.querySelector(
        'input[name="shippingType"]:checked'
    )?.value || 'registered';

    // 別の住所の入力内容を保持
    let draft = previous === 'new'
        ? { ...initialAddress }
        : {};


    // ----------------------------------------
    // 配送先選択に応じて入力欄を更新
    // ----------------------------------------

    function apply(type, switching) {

        // 「別の住所」から切り替える際に入力内容を保存
        if (switching && previous === 'new') {

            draft = Object.fromEntries(
                fields.map(input => [input.id, input.value])
            );
        }

        // 各入力欄を更新
        fields.forEach(input => {

            if (type === 'registered') {

                // 登録住所：編集不可
                input.readOnly = true;

                if (switching) {
                    input.value = registered[input.id] ?? '';
                }

            } else if (type === 'new') {

                // 別の住所：編集可能
                input.readOnly = false;

                if (switching) {
                    input.value = draft[input.id] ?? '';
                }
            }
        });

        // 説明文を変更
        const hint = document.getElementById('shipping-hint');

        if (hint) {

            hint.textContent = type === 'registered'
                ? '会員登録時の住所を使用します。別のお届け先を指定する場合は上の選択肢を切り替えてください。'
                : '今回のお届け先を入力してください。会員情報の住所は変更されません。';
        }

        previous = type;
    }


    // ----------------------------------------
    // ラジオボタンの切り替え
    // ----------------------------------------

    radios.forEach(radio => {

        radio.addEventListener('change', () => {

            if (radio.checked) {
                apply(radio.value, true);
            }
        });
    });


    // 初回表示
    apply(previous, false);
}
